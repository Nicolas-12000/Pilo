package com.pilo.documents;

import java.util.List;
import java.util.UUID;

public record DocumentResponse(
		UUID id,
		UUID requirementId,
		String requirementCode,
		String fileName,
		String mimeType,
		long fileSize,
		DocumentStatus status,
		DocumentExtractionResponse extraction,
		List<String> validationFailures,
		String processingFailureReason) {

	public static DocumentResponse from(Document document) {
		return from(document, null, List.of(), null);
	}

	public static DocumentResponse from(
			Document document,
			DocumentExtractionResponse extraction,
			List<String> validationFailures,
			String processingFailureReason) {
		return new DocumentResponse(
				document.getId(),
				document.getRequirement().getId(),
				document.getRequirement().getCode(),
				document.getFileName(),
				document.getMimeType(),
				document.getFileSize(),
				document.getStatus(),
				extraction,
				validationFailures == null ? List.of() : List.copyOf(validationFailures),
				processingFailureReason);
	}
}
