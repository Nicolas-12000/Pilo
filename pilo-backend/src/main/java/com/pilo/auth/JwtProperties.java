package com.pilo.auth;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "pilo.jwt")
public record JwtProperties(
		String secret, String previousSecret, Duration expiration, String issuer, String audience) {

	public JwtProperties(String secret, Duration expiration) {
		this(secret, null, expiration, "pilo", "pilo-api");
	}

	public JwtProperties {
		if (!StringUtils.hasText(issuer)) {
			issuer = "pilo";
		}
		if (!StringUtils.hasText(audience)) {
			audience = "pilo-api";
		}
		if (!StringUtils.hasText(previousSecret)) {
			previousSecret = null;
		}
	}

	public boolean hasPreviousSecret() {
		return previousSecret != null;
	}
}
