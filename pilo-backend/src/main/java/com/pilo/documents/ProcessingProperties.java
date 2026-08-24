package com.pilo.documents;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pilo.processing")
public record ProcessingProperties(String mode) {

	public boolean inline() {
		return mode == null || mode.isBlank() || "inline".equalsIgnoreCase(mode);
	}

	public boolean external() {
		return "external".equalsIgnoreCase(mode);
	}
}
