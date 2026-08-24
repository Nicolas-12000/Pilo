package com.pilo.documents;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pilo.storage")
public record StorageProperties(String localPath) {
}
