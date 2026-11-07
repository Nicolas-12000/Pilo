package com.pilo.auth;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "pilo.jwt")
public record JwtProperties(
		String secret,
		@DefaultValue String previousSecret,
		Duration expiration,
		@DefaultValue("pilo") String issuer,
		@DefaultValue("pilo-api") String audience) {

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

	/** Short form for unit tests (not used by Spring configuration binding). */
	public static JwtProperties forTest(String secret, Duration expiration) {
		return new JwtProperties(secret, null, expiration, "pilo", "pilo-api");
	}
}
