package com.pilo.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class GeminiStructuredExtractionClientTest {

	private final JsonMapper mapper = JsonMapper.builder().build();
	private final GeminiStructuredExtractionClient client =
			new GeminiStructuredExtractionClient(new AiProperties("gemini", "", "gemini-3.6-flash", 0.6), mapper);

	@Test
	void dropsFieldsWithoutEvidenceAndScalesConfidence() {
		var parsed = mapper.readTree(
				"""
				{
				  "documentType": "identity_document",
				  "confidence": 0.9,
				  "fields": {
				    "personName": {"value": "Ana García", "evidence": "ANA GARCÍA"},
				    "idNumber": {"value": "12345678Z", "evidence": "DNI 87654321X"},
				    "address": null
				  }
				}
				""");

		StructuredExtraction extraction = client.normalize(parsed);

		assertThat(extraction.documentType()).isEqualTo("identity_document");
		assertThat(extraction.confidence()).isEqualTo(0.45);
		assertThat(extraction.extractedFieldsJson()).contains("personName").doesNotContain("idNumber");
	}

	@Test
	void unknownTypeBecomesOther() {
		var parsed = mapper.readTree("""
				{"documentType": "spaceship_licence", "confidence": 1, "fields": {}}
				""");

		StructuredExtraction extraction = client.normalize(parsed);

		assertThat(extraction.documentType()).isEqualTo("other");
		assertThat(extraction.confidence()).isZero();
	}

	@Test
	void failsWithoutApiKeyInsteadOfMocking() {
		assertThatThrownBy(() -> client.extract(null)).isInstanceOf(IllegalStateException.class);
	}
}
