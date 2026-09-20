package com.pilo.documents.validation;

import com.pilo.ai.StructuredExtraction;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
public class IdNumberFormatRule implements DocumentRule {

	@Override
	public String code() {
		return "ID_NUMBER_FORMAT";
	}

	@Override
	public boolean evaluate(StructuredExtraction extraction, JsonNode extractedFields, ValidationRules rules) {
		if (!"identity_document".equals(rules.expectedDocumentType())) {
			return true;
		}
		String idNumber = extractedFields.path("idNumber").asString("").trim();
		if (idNumber.isBlank()) {
			return false;
		}
		return SpanishIdNumberValidator.isValid(idNumber);
	}
}
