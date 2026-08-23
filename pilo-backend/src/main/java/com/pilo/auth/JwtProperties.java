package com.pilo.auth;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pilo.jwt")
public record JwtProperties(String secret, Duration expiration) {
}
