package com.pilo.integrations;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class IntegrationServiceTest {

	private final IntegrationService integrationService =
			new IntegrationService(new IntegrationProperties(null, null, null, null, true, 120), JsonMapper.builder().build());

	@Test
	void parsesBoeLegislationResults() {
		String body =
				"""
				{
				  "status": {"code": "200", "text": "ok"},
				  "data": [
				    {
				      "identificador": "BOE-A-2026-15029",
				      "titulo": "Orden sobre procedimiento administrativo",
				      "url_html_consolidada": "https://www.boe.es/buscar/act.php?id=BOE-A-2026-15029"
				    }
				  ]
				}
				""";

		var results = integrationService.parseBoeLegislation(body);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).externalId()).isEqualTo("BOE-A-2026-15029");
		assertThat(results.get(0).demo()).isFalse();
	}

	@Test
	void parsesDatosCatalogItems() {
		String body =
				"""
				{
				  "items": [
				    {
				      "_about": "https://datos.gob.es/catalogo/demo",
				      "identifier": "demo-dataset",
				      "title": [{"_value": "Procedimientos administrativos"}],
				      "description": [{"_value": "Dataset sobre trámites administrativos"}]
				    }
				  ]
				}
				""";

		var results = integrationService.parseDatosCatalog(body);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).title()).contains("Procedimientos");
		assertThat(results.get(0).demo()).isFalse();
	}

	@Test
	void filtersSiaResultsByAdministrativeContext() {
		String body =
				"""
				{
				  "items": [
				    {
				      "_about": "https://datos.gob.es/catalogo/a",
				      "title": "Empresas registradas",
				      "description": "Datos mercantiles"
				    },
				    {
				      "_about": "https://datos.gob.es/catalogo/b",
				      "title": "Procedimientos SIA",
				      "description": "Relación de trámites administrativos"
				    }
				  ]
				}
				""";

		var results = integrationService.parseSiaCatalog(body);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).sourceName()).isEqualTo("SIA");
	}

	@Test
	void parsesDatosCatalogNestedUnderResult() {
		String body =
				"""
				{
				  "format": "linked-data-api",
				  "result": {
				    "items": [
				      {
				        "_about": "https://datos.gob.es/catalogo/alquiler",
				        "title": [{"_value": "Condiciones de alquiler", "_lang": "es"}]
				      }
				    ]
				  }
				}
				""";

		var results = integrationService.parseDatosCatalog(body);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).title()).isEqualTo("Condiciones de alquiler");
	}

	@Test
	void encodesBoeJsonQueryInUri() {
		var uri = integrationService.boeSearchUri("contrato \"alquiler\"", true);

		assertThat(uri.getRawQuery()).doesNotContain("{").doesNotContain("\"").doesNotContain(" ");
		assertThat(uri.getQuery()).contains("titulo:(contrato alquiler) and vigencia_agotada:N");
	}

	@Test
	void encodesSiaSearchLinkWithSpaces() {
		String url = integrationService.siaPagSearchUrl("Solicitud de empadronamiento");

		assertThat(url).endsWith("?texto=Solicitud%20de%20empadronamiento");
	}

	@Test
	void buildsBoeSearchQueryFromUserInput() {
		assertThat(IntegrationService.buildBoeSearchQuery("Solicitud de empadronamiento municipal", false))
				.contains("texto:(empadronamiento municipal)");
	}

	@Test
	void stripsQuerySyntaxFromUserInput() {
		assertThat(IntegrationService.buildBoeSearchQuery("alquiler) or titulo:(x", true))
				.contains("titulo:(alquiler titulo x) and vigencia_agotada:N");
	}
}
