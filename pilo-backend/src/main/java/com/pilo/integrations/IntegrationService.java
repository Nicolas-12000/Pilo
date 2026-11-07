package com.pilo.integrations;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class IntegrationService {

	private static final int MAX_RESULTS = 5;
	private static final Duration CACHE_TTL = Duration.ofHours(6);
	private static final int CACHE_MAX_ENTRIES = 500;
	private static final Set<String> STOPWORDS = Set.of(
			"de", "del", "la", "las", "el", "los", "y", "e", "o", "en", "a", "al", "por", "para", "con",
			"un", "una", "sobre", "solicitud", "tramite", "trámite", "and", "or", "not");

	private final Map<String, CachedResult> cache = new ConcurrentHashMap<>();

	private final IntegrationProperties properties;
	private final RestClient restClient;
	private final ObjectMapper objectMapper;

	public IntegrationService(IntegrationProperties properties, ObjectMapper objectMapper) {
		this.properties = properties;
		this.objectMapper = objectMapper;
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout((int) properties.timeout().toMillis());
		requestFactory.setReadTimeout((int) properties.timeout().toMillis());
		this.restClient = RestClient.builder().requestFactory(requestFactory).build();
	}

	public List<ExternalReferenceResponse> searchBoe(String query) {
		return cached("boe:" + query, () -> {
			try {
				// Title matches are far more relevant than full-text; full-text only as a fallback.
				List<ExternalReferenceResponse> byTitle = parseBoeLegislation(fetchBoeLegislation(query, true));
				if (!byTitle.isEmpty()) {
					return byTitle;
				}
				return parseBoeLegislation(fetchBoeLegislation(query, false));
			} catch (Exception ignored) {
				return List.of();
			}
		}, () -> demoFallback(
				"BOE",
				query,
				"https://www.boe.es/datosabiertos/",
				"Referencia normativa de ejemplo. Consulte el BOE Datos Abiertos para normativa consolidada."));
	}

	public List<ExternalReferenceResponse> searchDatos(String query) {
		return cached("datos:" + query, () -> {
			try {
				return parseDatosCatalog(fetchDatosCatalog(query));
			} catch (Exception ignored) {
				return List.of();
			}
		}, () -> demoFallback(
				"datos.gob.es",
				query,
				"https://datos.gob.es/",
				"Dataset de ejemplo del catálogo de datos abiertos."));
	}

	public List<ExternalReferenceResponse> searchSia(String query) {
		return cached("sia:" + query, () -> {
			try {
				return parseSiaCatalog(fetchDatosCatalog(query));
			} catch (Exception ignored) {
				return List.of();
			}
		}, () -> List.of(livePagReference(query)));
	}

	/** Caches live, non-empty results only, so an outage or the demo fallback is never pinned. */
	private List<ExternalReferenceResponse> cached(
			String key,
			Supplier<List<ExternalReferenceResponse>> live,
			Supplier<List<ExternalReferenceResponse>> fallback) {
		String normalizedKey = key.toLowerCase(Locale.ROOT).trim();
		CachedResult hit = cache.get(normalizedKey);
		if (hit != null && hit.expiresAt().isAfter(Instant.now())) {
			return hit.results();
		}
		List<ExternalReferenceResponse> results = live.get();
		if (results.isEmpty()) {
			return fallback.get();
		}
		if (cache.size() >= CACHE_MAX_ENTRIES) {
			cache.clear();
		}
		cache.put(normalizedKey, new CachedResult(results, Instant.now().plus(CACHE_TTL)));
		return results;
	}

	private record CachedResult(List<ExternalReferenceResponse> results, Instant expiresAt) {}

	String fetchBoeLegislation(String query, boolean titleOnly) {
		return restClient
				.get()
				.uri(boeSearchUri(query, titleOnly))
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.retrieve()
				.body(String.class);
	}

	URI boeSearchUri(String query, boolean titleOnly) {
		// The query is raw JSON; it must go through a URI variable so braces and quotes get encoded.
		return UriComponentsBuilder.fromUriString(properties.boeLegislationUrl())
				.queryParam("limit", MAX_RESULTS)
				.queryParam("query", "{query}")
				.encode()
				.buildAndExpand(Map.of("query", buildBoeSearchQuery(query, titleOnly)))
				.toUri();
	}

	String fetchDatosCatalog(String query) {
		String searchTerm = primarySearchTerm(query);
		return restClient
				.get()
				.uri(properties.datosCatalogUrl() + "/title/{title}?_pageSize={pageSize}&_page=0", searchTerm, MAX_RESULTS)
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.retrieve()
				.body(String.class);
	}

	List<ExternalReferenceResponse> parseBoeLegislation(String body) {
		if (body == null || body.isBlank()) {
			return List.of();
		}

		try {
			JsonNode root = objectMapper.readTree(body);
			JsonNode data = root.path("data");
			if (!data.isArray()) {
				return List.of();
			}

			List<ExternalReferenceResponse> results = new ArrayList<>();
			for (JsonNode item : data) {
				String identifier = item.path("identificador").asString(null);
				String title = item.path("titulo").asString(null);
				if (title == null || title.isBlank()) {
					continue;
				}
				String url = item.path("url_html_consolidada").asString(null);
				if (url == null || url.isBlank()) {
					url = identifier == null
							? "https://www.boe.es/datosabiertos/"
							: "https://www.boe.es/buscar/act.php?id=" + identifier;
				}
				results.add(ExternalReferenceResponse.live(
						"BOE",
						identifier == null ? title : identifier,
						truncate(title, 200),
						url,
						"Normativa consolidada publicada en el BOE."));
				if (results.size() >= MAX_RESULTS) {
					break;
				}
			}
			return results;
		} catch (Exception exception) {
			return List.of();
		}
	}

	List<ExternalReferenceResponse> parseDatosCatalog(String body) {
		return parseCatalogItems("datos.gob.es", body, false);
	}

	List<ExternalReferenceResponse> parseSiaCatalog(String body) {
		return parseCatalogItems("SIA", body, true);
	}

	private List<ExternalReferenceResponse> parseCatalogItems(
			String sourceName, String body, boolean siaFilter) {
		if (body == null || body.isBlank()) {
			return List.of();
		}

		try {
			JsonNode root = objectMapper.readTree(body);
			// The linked-data API nests results under "result"; keep the flat shape as a fallback.
			JsonNode items = root.path("result").path("items");
			if (!items.isArray()) {
				items = root.path("items");
			}
			if (!items.isArray()) {
				return List.of();
			}

			List<ExternalReferenceResponse> results = new ArrayList<>();
			for (JsonNode item : items) {
				String title = readTextValue(item.path("title"));
				if (title == null || title.isBlank()) {
					continue;
				}
				String description = readTextValue(item.path("description"));
				if (siaFilter && !matchesSiaContext(title, description)) {
					continue;
				}
				String identifier = readTextValue(item.path("identifier"));
				String url = item.path("_about").asString(null);
				if (url == null || url.isBlank()) {
					url = "https://datos.gob.es/";
				}
				results.add(ExternalReferenceResponse.live(
						sourceName,
						identifier == null ? title : identifier,
						title,
						url,
						description == null || description.isBlank()
								? "Dataset del catálogo de datos abiertos."
								: truncate(description, 240)));
				if (results.size() >= MAX_RESULTS) {
					break;
				}
			}
			return results;
		} catch (Exception exception) {
			return List.of();
		}
	}

	String siaPagSearchUrl(String query) {
		return UriComponentsBuilder.fromUriString(properties.siaPagSearchUrl())
				.queryParam("texto", "{texto}")
				.encode()
				.buildAndExpand(Map.of("texto", query))
				.toUriString();
	}

	private ExternalReferenceResponse livePagReference(String query) {
		String url = siaPagSearchUrl(query);
		return ExternalReferenceResponse.live(
				"SIA",
				"pag-tramites-search",
				"Buscador oficial de trámites administrativos",
				url,
				"El SIA no expone una API pública general. Enlace al buscador oficial de trámites de la Administración Pública.");
	}

	private List<ExternalReferenceResponse> demoFallback(
			String sourceName, String query, String url, String snippet) {
		if (!properties.demoFallbackEnabled()) {
			return List.of();
		}
		return List.of(ExternalReferenceResponse.demo(
				sourceName,
				"demo-" + URLEncoder.encode(query, StandardCharsets.UTF_8),
				sourceName + ": " + query,
				url,
				snippet + " Resultado de ejemplo (demo)."));
	}

	static String buildBoeSearchQuery(String query, boolean titleOnly) {
		// Letters, digits and spaces only: nothing can break out of the JSON or the query_string syntax.
		String sanitized = query.replaceAll("[^\\p{L}\\p{N} ]", " ").trim().replaceAll("\\s+", " ");
		String keywords = Arrays.stream(sanitized.split(" "))
				.filter(word -> !STOPWORDS.contains(word.toLowerCase(Locale.ROOT)))
				.collect(Collectors.joining(" "));
		String terms = keywords.isBlank() ? sanitized : keywords;
		String condition = titleOnly
				? "titulo:(" + terms + ") and vigencia_agotada:N"
				: "texto:(" + terms + ")";
		return "{\"query\":{\"query_string\":{\"query\":\"" + condition + "\"}},"
				+ "\"sort\":[{\"fecha_publicacion\":\"desc\"}]}";
	}

	static String primarySearchTerm(String query) {
		String[] words = query.trim().split("\\s+");
		for (String word : words) {
			if (word.length() >= 3) {
				return word.toLowerCase(Locale.ROOT);
			}
		}
		return query.trim().toLowerCase(Locale.ROOT);
	}

	static boolean matchesSiaContext(String title, String description) {
		String combined = ((title == null ? "" : title) + " " + (description == null ? "" : description))
				.toLowerCase(Locale.ROOT);
		return combined.contains("procedimiento")
				|| combined.contains("administrativ")
				|| combined.contains("tramit")
				|| combined.contains("sia");
	}

	static String readTextValue(JsonNode node) {
		if (node.isArray()) {
			return node.values().stream()
					.map(value -> value.path("_value").asString(value.asString(null)))
					.filter(value -> value != null && !value.isBlank())
					.collect(Collectors.joining(" "));
		}
		return node.asString(null);
	}

	private static String truncate(String value, int maxLength) {
		if (value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, maxLength - 1).trim() + "…";
	}
}
