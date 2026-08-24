package com.pilo.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class AiClientConfiguration {

	@Bean
	@ConditionalOnProperty(name = "pilo.ai.provider", havingValue = "gemini")
	StructuredExtractionClient geminiStructuredExtractionClient(AiProperties aiProperties, ObjectMapper objectMapper) {
		return new GeminiStructuredExtractionClient(aiProperties, objectMapper);
	}

	@Bean
	@ConditionalOnProperty(name = "pilo.ai.provider", havingValue = "mock", matchIfMissing = true)
	StructuredExtractionClient mockStructuredExtractionClient() {
		return new MockStructuredExtractionClient();
	}
}
