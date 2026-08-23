package com.pilo.users;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pilo.seed")
public record SeedProperties(boolean enabled) {
}
