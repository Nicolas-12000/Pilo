package com.pilo.documents.validation;

import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class ValidationRulesParser {

	private final ObjectMapper objectMapper;

	public ValidationRulesParser(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public ValidationRules parse(String rawRules) {
		if (rawRules == null || rawRules.isBlank()) {
			return ValidationRules.empty();
		}
		try {
			JsonNode node = objectMapper.readTree(rawRules);
			return new ValidationRules(
					node.path("expectedDocumentType").asString(""),
					node.path("requireFutureExpiration").asBoolean(false),
					node.path("minConfidence").isMissingNode() || node.path("minConfidence").isNull()
							? null
							: node.path("minConfidence").asDouble());
		} catch (Exception exception) {
			return ValidationRules.empty();
		}
	}

	public String serialize(ValidationRulesRequest request) {
		try {
			return objectMapper.writeValueAsString(
					new ValidationRules(
							request.expectedDocumentType(),
							request.requireFutureExpiration(),
							request.minConfidence()));
		} catch (Exception exception) {
			throw new IllegalArgumentException("INVALID_VALIDATION_RULES");
		}
	}

	public record ValidationRulesRequest(
			String expectedDocumentType, boolean requireFutureExpiration, Double minConfidence) {}
}
