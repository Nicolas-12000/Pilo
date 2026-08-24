package com.pilo.documents.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

public interface ObjectStorage {

	String provider();

	PresignedUpload presignUpload(UploadInstruction instruction);

	StoredContent storeInline(InlineUpload upload);

	StoredContent open(String storageKey);

	record UploadInstruction(
			UUID caseId, UUID documentId, String fileName, String mimeType, long fileSize) {}

	record InlineUpload(UUID caseId, UUID documentId, String fileName, String mimeType, byte[] content) {}

	record PresignedUpload(
			String uploadUrl, String method, String storageKey, Map<String, String> headers) {}

	record StoredContent(String storageKey, InputStream content, String mimeType) implements AutoCloseable {
		@Override
		public void close() throws IOException {
			content.close();
		}
	}
}
