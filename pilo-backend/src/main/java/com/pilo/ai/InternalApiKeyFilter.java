package com.pilo.ai;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

	private final InternalApiProperties internalApiProperties;

	public InternalApiKeyFilter(InternalApiProperties internalApiProperties) {
		this.internalApiProperties = internalApiProperties;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getRequestURI().startsWith("/internal/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String apiKey = request.getHeader("X-Internal-Api-Key");
		if (apiKey == null || !constantTimeEquals(apiKey, internalApiProperties.apiKey())) {
			response.sendError(HttpStatus.UNAUTHORIZED.value());
			return;
		}
		filterChain.doFilter(request, response);
	}

	private static boolean constantTimeEquals(String provided, String expected) {
		if (expected == null) {
			return false;
		}
		return MessageDigest.isEqual(
				provided.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
	}
}
