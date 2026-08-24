package com.pilo.documents.validation;

import com.pilo.ai.StructuredExtraction;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
public class ExpectedDocumentTypeRule implements DocumentRule {

	@Override
	public boolean evaluate(StructuredExtraction extraction, JsonNode extractedFields, ValidationRules rules) {
		if (rules.expectedDocumentType() == null || rules.expectedDocumentType().isBlank()) {
			return true;
		}
		return rules.expectedDocumentType().equals(extraction.documentType());
	}
}
