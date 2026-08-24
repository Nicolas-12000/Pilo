package com.pilo.documents.storage;

import com.pilo.documents.StorageProperties;
import java.util.Map;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

public class S3ObjectStorage implements ObjectStorage {

	private final StorageProperties storageProperties;
	private final S3Presigner s3Presigner;
	private final software.amazon.awssdk.services.s3.S3Client s3Client;

	public S3ObjectStorage(
			StorageProperties storageProperties,
			S3Presigner s3Presigner,
			software.amazon.awssdk.services.s3.S3Client s3Client) {
		this.storageProperties = storageProperties;
		this.s3Presigner = s3Presigner;
		this.s3Client = s3Client;
	}

	@Override
	public String provider() {
		return "s3";
	}

	@Override
	public PresignedUpload presignUpload(UploadInstruction instruction) {
		String storageKey = StorageKeyFactory.build(
				instruction.caseId(), instruction.documentId(), instruction.fileName());
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(storageProperties.bucket())
				.key(storageKey)
				.contentType(instruction.mimeType())
				.contentLength(instruction.fileSize())
				.build();
		var presignedRequest = PutObjectPresignRequest.builder()
				.signatureDuration(storageProperties.presignedUrlExpiration())
				.putObjectRequest(putObjectRequest)
				.build();
		var presigned = s3Presigner.presignPutObject(presignedRequest);
		return new PresignedUpload(
				presigned.url().toString(),
				"PUT",
				storageKey,
				Map.of("Content-Type", instruction.mimeType()));
	}

	@Override
	public StoredContent storeInline(InlineUpload upload) {
		String storageKey = StorageKeyFactory.build(upload.caseId(), upload.documentId(), upload.fileName());
		s3Client.putObject(builder -> builder.bucket(storageProperties.bucket())
				.key(storageKey)
				.contentType(upload.mimeType())
				.build(), software.amazon.awssdk.core.sync.RequestBody.fromBytes(upload.content()));
		return open(storageKey);
	}

	@Override
	public StoredContent open(String storageKey) {
		var response = s3Client.getObject(GetObjectRequest.builder()
				.bucket(storageProperties.bucket())
				.key(storageKey)
				.build());
		String mimeType = response.response().contentType() != null
				? response.response().contentType()
				: "application/octet-stream";
		return new StoredContent(storageKey, response, mimeType);
	}
}
