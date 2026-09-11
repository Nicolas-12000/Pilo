package com.pilo.integrations;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
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
		String normalized = normalizeQuery(query);
		if (normalized.isBlank()) {
			return List.of();
		}

		try {
			String body = restClient
					.get()
					.uri(properties.boeBaseUrl() + "/{date}", "20260101")
					.retrieve()
					.body(String.class);
			if (body != null && body.toLowerCase(Locale.ROOT).contains(normalized.toLowerCase(Locale.ROOT))) {
				return List.of(ExternalReferenceResponse.of(
						"BOE",
						"boe-sumario-20260101",
						"Referencia normativa relacionada con " + normalized,
						"https://www.boe.es/datosabiertos/",
						"Resultado encontrado en el sumario abierto del BOE."));
			}
		} catch (Exception ignored) {
			// Fall back below when the remote API is unavailable.
		}

		return demoFallback(
				"BOE",
				normalized,
				"https://www.boe.es/",
				"Fuente normativa pública de referencia para el trámite.");
	}

	public List<ExternalReferenceResponse> searchDatos(String query) {
		return searchCatalog("datos.gob.es", properties.datosBaseUrl(), query, "Catálogo de datos abiertos");
	}

	public List<ExternalReferenceResponse> searchSia(String query) {
		return searchCatalog(
				"SIA",
				properties.siaBaseUrl(),
				"procedimiento administrativo " + query,
				"Referencia del Sistema de Información Administrativa");
	}

	private List<ExternalReferenceResponse> searchCatalog(
			String sourceName, String baseUrl, String query, String sourceLabel) {
		String normalized = normalizeQuery(query);
		if (normalized.isBlank()) {
			return List.of();
		}

		try {
			String body = restClient
					.get()
					.uri(baseUrl + "?q={query}&limit={limit}", normalized, MAX_RESULTS)
					.retrieve()
					.body(String.class);
			List<ExternalReferenceResponse> parsed = parseCatalogResults(sourceName, body);
			if (!parsed.isEmpty()) {
				return parsed;
			}
		} catch (Exception ignored) {
			// Fall back below when the remote API is unavailable.
		}

		return demoFallback(
				sourceName,
				normalized,
				"https://datos.gob.es/",
				sourceLabel + " relacionado con " + normalized + ".");
	}

	private List<ExternalReferenceResponse> parseCatalogResults(String sourceName, String body) {
		if (body == null || body.isBlank()) {
			return List.of();
		}

		try {
			JsonNode root = objectMapper.readTree(body);
			JsonNode items = root.path("result").path("items");
			if (!items.isArray()) {
				return List.of();
			}

			List<ExternalReferenceResponse> results = new ArrayList<>();
			for (JsonNode item : items) {
				String title = item.path("title").asString(null);
				if (title == null || title.isBlank()) {
					continue;
				}
				String identifier = item.path("identifier").asString(null);
				String url = item.path("_about").asString(null);
				String description = item.path("description").asString(null);
				results.add(ExternalReferenceResponse.of(
						sourceName,
						identifier == null ? title : identifier,
						title,
						url,
						description));
				if (results.size() >= MAX_RESULTS) {
					break;
				}
			}
			return results;
		} catch (Exception exception) {
			return List.of();
		}
	}

	private List<ExternalReferenceResponse> demoFallback(
			String sourceName, String query, String url, String snippet) {
		if (!properties.demoFallbackEnabled()) {
			return List.of();
		}
		return List.of(ExternalReferenceResponse.of(
				sourceName,
				"demo-" + URLEncoder.encode(query, StandardCharsets.UTF_8),
				sourceName + ": " + query,
				url,
				snippet));
	}

	private static String normalizeQuery(String query) {
		return query == null ? "" : query.trim();
	}
}
