package com.pilo.documents;

import static org.assertj.core.api.Assertions.assertThat;

import com.pilo.ai.AiProperties;
import com.pilo.ai.StructuredExtraction;
import com.pilo.documents.validation.ConfidenceRule;
import com.pilo.documents.validation.ExpectedDocumentTypeRule;
import com.pilo.documents.validation.FutureExpirationRule;
import com.pilo.documents.validation.ValidationRulesParser;
import com.pilo.procedures.Requirement;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class DocumentValidationServiceTest {

	private DocumentValidationService documentValidationService;

	@BeforeEach
	void setUp() {
		JsonMapper objectMapper = JsonMapper.builder().build();
		documentValidationService = new DocumentValidationService(
				new ValidationRulesParser(objectMapper),
				objectMapper,
				List.of(
						new ConfidenceRule(new AiProperties("mock", "", "gemini-2.0-flash", 0.5)),
						new ExpectedDocumentTypeRule(),
						new FutureExpirationRule()));
	}

	@Test
	void acceptsMatchingTypeAndFutureExpiration() {
		boolean valid = documentValidationService.isValid(
				new StructuredExtraction(
						"rental_contract",
						0.9,
						"{\"expirationDate\":\"2099-01-01\"}"),
				requirement("{\"expectedDocumentType\":\"rental_contract\",\"requireFutureExpiration\":true}"));

		assertThat(valid).isTrue();
	}

	@Test
	void rejectsUnexpectedDocumentType() {
		boolean valid = documentValidationService.isValid(
				new StructuredExtraction("identity_document", 0.9, "{}"),
				requirement("{\"expectedDocumentType\":\"rental_contract\"}"));

		assertThat(valid).isFalse();
	}

	@Test
	void rejectsLowConfidence() {
		boolean valid = documentValidationService.isValid(
				new StructuredExtraction("identity_document", 0.2, "{}"),
				requirement("{\"expectedDocumentType\":\"identity_document\"}"));

		assertThat(valid).isFalse();
	}

	private static Requirement requirement(String rules) {
		Requirement requirement = new Requirement();
		requirement.setValidationRules(rules);
		return requirement;
	}
}
