package com.pilo.documents;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pilo.storage")
public record StorageProperties(
		String provider,
		String localPath,
		String bucket,
		String region,
		String endpoint,
		Duration presignedUrlExpiration) {

	public StorageProperties {
		if (provider == null || provider.isBlank()) {
			provider = "local";
		}
		if (localPath == null || localPath.isBlank()) {
			localPath = "./storage";
		}
		if (presignedUrlExpiration == null) {
			presignedUrlExpiration = Duration.ofMinutes(15);
		}
	}
}
