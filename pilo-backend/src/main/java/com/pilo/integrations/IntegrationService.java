package com.pilo.integrations;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
		try {
			List<ExternalReferenceResponse> parsed = parseBoeLegislation(fetchBoeLegislation(query));
			if (!parsed.isEmpty()) {
				return parsed;
			}
		} catch (Exception ignored) {
			// Fall back below when the remote API is unavailable.
		}

		return demoFallback(
				"BOE",
				query,
				"https://www.boe.es/datosabiertos/",
				"Referencia normativa de ejemplo. Consulte el BOE Datos Abiertos para normativa consolidada.");
	}

	public List<ExternalReferenceResponse> searchDatos(String query) {
		try {
			List<ExternalReferenceResponse> parsed = parseDatosCatalog(fetchDatosCatalog(query));
			if (!parsed.isEmpty()) {
				return parsed;
			}
		} catch (Exception ignored) {
			// Fall back below when the remote API is unavailable.
		}

		return demoFallback(
				"datos.gob.es",
				query,
				"https://datos.gob.es/",
				"Dataset de ejemplo del catálogo de datos abiertos.");
	}

	public List<ExternalReferenceResponse> searchSia(String query) {
		try {
			List<ExternalReferenceResponse> parsed = parseSiaCatalog(fetchDatosCatalog(query));
			if (!parsed.isEmpty()) {
				return parsed;
			}
		} catch (Exception ignored) {
			// Fall back below when the remote API is unavailable.
		}

		return List.of(livePagReference(query));
	}

	String fetchBoeLegislation(String query) {
		String searchQuery = buildBoeSearchQuery(query);
		return restClient
				.get()
				.uri(UriComponentsBuilder.fromUriString(properties.boeLegislationUrl())
						.queryParam("limit", MAX_RESULTS)
						.queryParam("query", searchQuery)
						.build(true)
						.toUri())
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.retrieve()
				.body(String.class);
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
						title,
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
			JsonNode items = root.path("items");
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

	private ExternalReferenceResponse livePagReference(String query) {
		String url = UriComponentsBuilder.fromUriString(properties.siaPagSearchUrl())
				.queryParam("texto", query)
				.build(true)
				.toUriString();
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

	static String buildBoeSearchQuery(String query) {
		String sanitized = query.trim().replaceAll("\\s+", " ");
		String escaped = sanitized.replace("\"", "\\\"");
		return "{\"query\":{\"query_string\":{\"query\":\"texto:" + escaped + "\"}},"
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
