package com.pilo.documents;

import java.util.UUID;

public record DocumentResponse(
		UUID id,
		UUID requirementId,
		String requirementCode,
		String fileName,
		String mimeType,
		long fileSize,
		DocumentStatus status) {

	public static DocumentResponse from(Document document) {
		return new DocumentResponse(
				document.getId(),
				document.getRequirement().getId(),
				document.getRequirement().getCode(),
				document.getFileName(),
				document.getMimeType(),
				document.getFileSize(),
				document.getStatus());
	}
}
