package com.pilo.integrations;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class IntegrationService {

	private final RestClient restClient = RestClient.builder().build();

	public List<ExternalReferenceResponse> searchBoe(String query) {
		try {
			// Public BOE search endpoint (best-effort for demo).
			String body = restClient
					.get()
					.uri("https://www.boe.es/datosabiertos/api/boe/sumario/{date}", "20260101")
					.retrieve()
					.body(String.class);
			if (body != null && body.toLowerCase().contains(query.toLowerCase())) {
				return List.of(new ExternalReferenceResponse("BOE", "demo-reference", "Referencia BOE relacionada con " + query));
			}
		} catch (Exception ignored) {
			// Fall back to demo payload when remote API is unavailable.
		}
		return List.of(new ExternalReferenceResponse(
				"BOE",
				"demo-" + query,
				"Fuente normativa pública de referencia para el trámite (" + query + ")."));
	}

	public List<ExternalReferenceResponse> searchDatos(String query) {
		try {
			String body = restClient
					.get()
					.uri("https://datos.gob.es/apidata/catalog/dataset?q={query}", query)
					.retrieve()
					.body(String.class);
			if (body != null && !body.isBlank()) {
				return List.of(new ExternalReferenceResponse("datos.gob.es", query, "Dataset público relacionado con " + query));
			}
		} catch (Exception ignored) {
			// Fall back to demo payload when remote API is unavailable.
		}
		return List.of(new ExternalReferenceResponse(
				"datos.gob.es", query, "Catálogo público de datos administrativos (" + query + ")."));
	}
}
