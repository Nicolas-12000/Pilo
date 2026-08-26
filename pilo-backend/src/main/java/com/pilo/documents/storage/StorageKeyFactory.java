package com.pilo.documents.storage;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StorageKeyFactory {

	private static final Pattern DOCUMENT_ID_PATTERN =
			Pattern.compile("^cases/[0-9a-f-]{36}/documents/([0-9a-f-]{36})/.+$");

	private StorageKeyFactory() {}

	public static String build(UUID caseId, UUID documentId, String fileName) {
		return "cases/%s/documents/%s/%s".formatted(caseId, documentId, sanitize(fileName));
	}

	public static UUID parseDocumentId(String storageKey) {
		Matcher matcher = DOCUMENT_ID_PATTERN.matcher(storageKey);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("INVALID_STORAGE_KEY");
		}
		return UUID.fromString(matcher.group(1));
	}

	private static String sanitize(String fileName) {
		if (fileName == null || fileName.isBlank()) {
			return "upload.bin";
		}
		// Strip any directory components the client may have sent; only the leaf name is kept.
		String baseName = fileName.replace('\\', '/');
		int lastSlash = baseName.lastIndexOf('/');
		if (lastSlash >= 0) {
			baseName = baseName.substring(lastSlash + 1);
		}
		String sanitized = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
		// Reject filenames that are only dots (".", "..", "...") to prevent path traversal.
		if (sanitized.isBlank() || sanitized.chars().allMatch(c -> c == '.')) {
			return "upload.bin";
		}
		return sanitized;
	}
}
