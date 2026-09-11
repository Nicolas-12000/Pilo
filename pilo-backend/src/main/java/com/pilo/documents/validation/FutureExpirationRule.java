package com.pilo.documents.validation;

import com.pilo.ai.StructuredExtraction;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
public class FutureExpirationRule implements DocumentRule {

	@Override
	public String code() {
		return "FUTURE_EXPIRATION";
	}

	@Override
	public boolean evaluate(StructuredExtraction extraction, JsonNode extractedFields, ValidationRules rules) {
		if (!rules.requireFutureExpiration()) {
			return true;
		}
		String expirationDate = extractedFields.path("expirationDate").asString("");
		if (expirationDate.isBlank()) {
			return false;
		}
		try {
			return LocalDate.parse(expirationDate).isAfter(LocalDate.now());
		} catch (Exception exception) {
			return false;
		}
	}
}
