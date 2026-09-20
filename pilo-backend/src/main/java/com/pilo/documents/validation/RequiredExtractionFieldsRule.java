package com.pilo.documents.validation;

import com.pilo.ai.StructuredExtraction;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
public class RequiredExtractionFieldsRule implements DocumentRule {

	@Override
	public String code() {
		return "REQUIRED_FIELDS";
	}

	@Override
	public boolean evaluate(StructuredExtraction extraction, JsonNode extractedFields, ValidationRules rules) {
		if (!"identity_document".equals(rules.expectedDocumentType())) {
			return true;
		}
		String personName = extractedFields.path("personName").asString("").trim();
		String idNumber = extractedFields.path("idNumber").asString("").trim();
		return !personName.isBlank() && !idNumber.isBlank();
	}
}
