package com.pilo.documents.validation;

import com.pilo.ai.StructuredExtraction;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
public class DateCoherenceRule implements DocumentRule {

	@Override
	public String code() {
		return "DATE_COHERENCE";
	}

	@Override
	public boolean evaluate(StructuredExtraction extraction, JsonNode extractedFields, ValidationRules rules) {
		String issueDate = extractedFields.path("issueDate").asString("").trim();
		String expirationDate = extractedFields.path("expirationDate").asString("").trim();
		if (issueDate.isBlank() || expirationDate.isBlank()) {
			return true;
		}
		try {
			LocalDate issue = LocalDate.parse(issueDate);
			LocalDate expiration = LocalDate.parse(expirationDate);
			return !issue.isAfter(expiration);
		} catch (Exception exception) {
			return false;
		}
	}
}
