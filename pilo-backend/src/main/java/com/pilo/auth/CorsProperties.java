package com.pilo.auth;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pilo.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
