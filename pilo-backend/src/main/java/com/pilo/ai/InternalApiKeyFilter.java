package com.pilo.ai;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
		if (apiKey == null || !apiKey.equals(internalApiProperties.apiKey())) {
			response.sendError(HttpStatus.UNAUTHORIZED.value());
			return;
		}
		filterChain.doFilter(request, response);
	}
}
