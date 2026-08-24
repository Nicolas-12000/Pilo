package com.pilo.documents.validation;

import com.pilo.ai.AiProperties;
import com.pilo.ai.StructuredExtraction;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
public class ConfidenceRule implements DocumentRule {

	private final AiProperties aiProperties;

	public ConfidenceRule(AiProperties aiProperties) {
		this.aiProperties = aiProperties;
	}

	@Override
	public boolean evaluate(StructuredExtraction extraction, JsonNode extractedFields, ValidationRules rules) {
		double threshold = rules.minConfidence() != null ? rules.minConfidence() : aiProperties.minConfidence();
		return extraction.confidence() >= threshold;
	}
}
