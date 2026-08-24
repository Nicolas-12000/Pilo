package com.pilo.documents;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LocalDocumentStorage {

	private final StorageProperties storageProperties;

	public LocalDocumentStorage(StorageProperties storageProperties) {
		this.storageProperties = storageProperties;
	}

	public String store(UUID caseId, String originalFileName, byte[] content) throws IOException {
		Path caseDirectory = Path.of(storageProperties.localPath(), caseId.toString());
		Files.createDirectories(caseDirectory);
		String storageKey = UUID.randomUUID() + "-" + sanitize(originalFileName);
		Files.write(caseDirectory.resolve(storageKey), content);
		return storageKey;
	}

	public Path resolve(UUID caseId, String storageKey) {
		return Path.of(storageProperties.localPath(), caseId.toString(), storageKey);
	}

	private String sanitize(String fileName) {
		return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
	}
}
