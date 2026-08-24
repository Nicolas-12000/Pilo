package com.pilo.documents.validation;

import com.pilo.ai.StructuredExtraction;
import tools.jackson.databind.JsonNode;

public interface DocumentRule {

	boolean evaluate(StructuredExtraction extraction, JsonNode extractedFields, ValidationRules rules);
}
