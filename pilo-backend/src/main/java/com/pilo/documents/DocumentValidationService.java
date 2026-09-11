package com.pilo.documents;

import com.pilo.ai.StructuredExtraction;
import com.pilo.documents.validation.DocumentRule;
import com.pilo.documents.validation.ValidationResult;
import com.pilo.documents.validation.ValidationRules;
import com.pilo.documents.validation.ValidationRulesParser;
import com.pilo.procedures.Requirement;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class DocumentValidationService {

	private final ValidationRulesParser validationRulesParser;
	private final ObjectMapper objectMapper;
	private final List<DocumentRule> rules;

	public DocumentValidationService(
			ValidationRulesParser validationRulesParser, ObjectMapper objectMapper, List<DocumentRule> rules) {
		this.validationRulesParser = validationRulesParser;
		this.objectMapper = objectMapper;
		this.rules = List.copyOf(rules);
	}

	public ValidationResult validate(StructuredExtraction extraction, Requirement requirement) {
		ValidationRules parsed = validationRulesParser.parse(requirement.getValidationRules());
		JsonNode fields = parseFields(extraction.extractedFieldsJson());
		List<String> failures = new ArrayList<>();
		for (DocumentRule rule : rules) {
			if (!rule.evaluate(extraction, fields, parsed)) {
				failures.add(rule.code());
			}
		}
		return failures.isEmpty() ? ValidationResult.success() : ValidationResult.failure(failures);
	}

	public boolean isValid(StructuredExtraction extraction, Requirement requirement) {
		return validate(extraction, requirement).valid();
	}

	private JsonNode parseFields(String extractedFieldsJson) {
		try {
			return objectMapper.readTree(extractedFieldsJson);
		} catch (Exception exception) {
			return objectMapper.createObjectNode();
		}
	}
}
