package com.pilo.documents;

import static org.assertj.core.api.Assertions.assertThat;

import com.pilo.ai.AiProperties;
import com.pilo.ai.StructuredExtraction;
import com.pilo.documents.validation.ConfidenceRule;
import com.pilo.documents.validation.DateCoherenceRule;
import com.pilo.documents.validation.ExpectedDocumentTypeRule;
import com.pilo.documents.validation.FutureExpirationRule;
import com.pilo.documents.validation.IdNumberFormatRule;
import com.pilo.documents.validation.RequiredExtractionFieldsRule;
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
						new ConfidenceRule(new AiProperties("mock", "", "gemini-3.6-flash", 0.5)),
						new ExpectedDocumentTypeRule(),
						new RequiredExtractionFieldsRule(),
						new IdNumberFormatRule(),
						new FutureExpirationRule(),
						new DateCoherenceRule()));
	}

	@Test
	void acceptsMatchingTypeAndFutureExpiration() {
		boolean valid = documentValidationService.isValid(
				new StructuredExtraction(
						"rental_contract",
						0.9,
						"{\"issueDate\":\"2026-01-01\",\"expirationDate\":\"2099-01-01\"}"),
				requirement("{\"expectedDocumentType\":\"rental_contract\",\"requireFutureExpiration\":true}"));

		assertThat(valid).isTrue();
	}

	@Test
	void rejectsUnexpectedDocumentTypeWithFailureCode() {
		var result = documentValidationService.validate(
				new StructuredExtraction("identity_document", 0.9, "{}"),
				requirement("{\"expectedDocumentType\":\"rental_contract\"}"));

		assertThat(result.valid()).isFalse();
		assertThat(result.failures()).containsExactly("EXPECTED_DOCUMENT_TYPE");
	}

	@Test
	void rejectsLowConfidence() {
		boolean valid = documentValidationService.isValid(
				new StructuredExtraction("identity_document", 0.2, "{}"),
				requirement("{\"expectedDocumentType\":\"identity_document\"}"));

		assertThat(valid).isFalse();
	}

	@Test
	void rejectsInvalidSpanishIdNumberForIdentityDocument() {
		var result = documentValidationService.validate(
				new StructuredExtraction(
						"identity_document",
						0.95,
						"{\"personName\":\"Ana Usuario\",\"idNumber\":\"12345678X\",\"issueDate\":\"2026-01-01\",\"expirationDate\":\"2027-12-31\"}"),
				requirement("{\"expectedDocumentType\":\"identity_document\",\"minConfidence\":0.7}"));

		assertThat(result.valid()).isFalse();
		assertThat(result.failures()).contains("ID_NUMBER_FORMAT");
	}

	@Test
	void rejectsIncoherentDates() {
		var result = documentValidationService.validate(
				new StructuredExtraction(
						"rental_contract",
						0.95,
						"{\"issueDate\":\"2027-01-01\",\"expirationDate\":\"2026-01-01\"}"),
				requirement("{\"expectedDocumentType\":\"rental_contract\"}"));

		assertThat(result.valid()).isFalse();
		assertThat(result.failures()).contains("DATE_COHERENCE");
	}

	private static Requirement requirement(String rules) {
		Requirement requirement = new Requirement();
		requirement.setValidationRules(rules);
		return requirement;
	}
}
