package com.pilo.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pilo.internal")
public record InternalApiProperties(String apiKey) {
}
