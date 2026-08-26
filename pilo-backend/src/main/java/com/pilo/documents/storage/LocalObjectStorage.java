package com.pilo.documents.storage;

import com.pilo.documents.StorageProperties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class LocalObjectStorage implements ObjectStorage {

	private final StorageProperties storageProperties;

	public LocalObjectStorage(StorageProperties storageProperties) {
		this.storageProperties = storageProperties;
	}

	@Override
	public String provider() {
		return "local";
	}

	@Override
	public PresignedUpload presignUpload(UploadInstruction instruction) {
		String storageKey = StorageKeyFactory.build(
				instruction.caseId(), instruction.documentId(), instruction.fileName());
		return new PresignedUpload(
				"/api/v1/cases/" + instruction.caseId() + "/documents/upload",
				"POST",
				storageKey,
				Map.of());
	}

	@Override
	public StoredContent storeInline(InlineUpload upload) {
		String storageKey = StorageKeyFactory.build(upload.caseId(), upload.documentId(), upload.fileName());
		Path target = resolvePath(storageKey);
		try {
			Files.createDirectories(target.getParent());
			Files.write(target, upload.content());
		} catch (IOException exception) {
			throw new StorageException("LOCAL_STORE_FAILED", exception);
		}
		return new StoredContent(storageKey, new ByteArrayInputStream(upload.content()), upload.mimeType());
	}

	@Override
	public StoredContent open(String storageKey) {
		Path target = resolvePath(storageKey);
		try {
			byte[] bytes = Files.readAllBytes(target);
			return new StoredContent(storageKey, new ByteArrayInputStream(bytes), "application/octet-stream");
		} catch (IOException exception) {
			throw new StorageException("LOCAL_READ_FAILED", exception);
		}
	}

	private Path resolvePath(String storageKey) {
		Path base = Path.of(storageProperties.localPath()).toAbsolutePath().normalize();
		Path target = base.resolve(storageKey).normalize();
		if (!target.startsWith(base)) {
			throw new StorageException("INVALID_STORAGE_KEY", null);
		}
		return target;
	}
}
