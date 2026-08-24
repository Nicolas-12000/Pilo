package com.pilo.documents.validation;

public record ValidationRules(String expectedDocumentType, boolean requireFutureExpiration, Double minConfidence) {

	public static ValidationRules empty() {
		return new ValidationRules("", false, null);
	}
}
