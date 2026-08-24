package com.pilo.documents.storage;

import com.pilo.documents.StorageProperties;
import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class StorageConfiguration {

	@Bean
	@ConditionalOnProperty(name = "pilo.storage.provider", havingValue = "s3")
	S3Client s3Client(StorageProperties storageProperties) {
		return S3Client.builder()
				.region(Region.of(storageProperties.region()))
				.credentialsProvider(DefaultCredentialsProvider.create())
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(false).build())
				.build();
	}

	@Bean
	@ConditionalOnProperty(name = "pilo.storage.provider", havingValue = "s3")
	S3Presigner s3Presigner(StorageProperties storageProperties) {
		var builder = S3Presigner.builder()
				.region(Region.of(storageProperties.region()))
				.credentialsProvider(DefaultCredentialsProvider.create())
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(false).build());
		if (storageProperties.endpoint() != null && !storageProperties.endpoint().isBlank()) {
			builder.endpointOverride(URI.create(storageProperties.endpoint()));
		}
		return builder.build();
	}

	@Bean
	@ConditionalOnProperty(name = "pilo.storage.provider", havingValue = "local", matchIfMissing = true)
	ObjectStorage localObjectStorage(StorageProperties storageProperties) {
		return new LocalObjectStorage(storageProperties);
	}

	@Bean
	@ConditionalOnProperty(name = "pilo.storage.provider", havingValue = "s3")
	ObjectStorage s3ObjectStorage(
			StorageProperties storageProperties, S3Presigner s3Presigner, S3Client s3Client) {
		return new S3ObjectStorage(storageProperties, s3Presigner, s3Client);
	}
}
