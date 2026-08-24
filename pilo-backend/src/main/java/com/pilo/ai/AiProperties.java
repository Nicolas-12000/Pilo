package com.pilo.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pilo.ai")
public record AiProperties(String provider, String geminiApiKey, String model, double minConfidence) {
}
