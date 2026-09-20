package com.pilo.integrations;

import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class IntegrationQueryValidator {

	private static final Pattern ALLOWED_QUERY = Pattern.compile("^[\\p{L}\\p{N}\\s.,\\-'/()]+$");

	private final IntegrationProperties properties;

	public IntegrationQueryValidator(IntegrationProperties properties) {
		this.properties = properties;
	}

	public String requireValid(String query) {
		if (query == null || query.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_QUERY");
		}
		String normalized = query.trim().replaceAll("\\s+", " ");
		if (normalized.length() > properties.maxQueryLength()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "QUERY_TOO_LONG");
		}
		if (!ALLOWED_QUERY.matcher(normalized).matches()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_QUERY");
		}
		return normalized;
	}
}
