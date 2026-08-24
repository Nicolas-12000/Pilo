package com.pilo.ai;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class GeminiStructuredExtractionClient implements StructuredExtractionClient {

	private final AiProperties aiProperties;
	private final ObjectMapper objectMapper;
	private final RestClient restClient;

	public GeminiStructuredExtractionClient(AiProperties aiProperties, ObjectMapper objectMapper) {
		this.aiProperties = aiProperties;
		this.objectMapper = objectMapper;
		this.restClient = RestClient.builder().build();
	}

	@Override
	public StructuredExtraction extract(ExtractionInput input) {
		if (aiProperties.geminiApiKey() == null || aiProperties.geminiApiKey().isBlank()) {
			return new MockStructuredExtractionClient().extract(input);
		}

		try {
			String base64 = Base64.getEncoder().encodeToString(input.content());
			Map<String, Object> body = Map.of(
					"contents",
					List.of(Map.of(
							"parts",
							List.of(
									Map.of(
											"text",
											"""
											Extract structured administrative document data.
											Return JSON with keys documentType, confidence, extractedFields.
											extractedFields may include personName, idNumber, issueDate, expirationDate, address.
											Requirement code: %s
											Expected document type: %s
											"""
													.formatted(
															input.hint().requirementCode(),
															input.hint().expectedDocumentType())),
									Map.of(
											"inline_data",
											Map.of("mime_type", input.mimeType(), "data", base64))))),
					"generationConfig",
					Map.of("responseMimeType", "application/json"));

			String responseBody = restClient
					.post()
					.uri("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s"
							.formatted(aiProperties.model(), aiProperties.geminiApiKey()))
					.contentType(MediaType.APPLICATION_JSON)
					.body(body)
					.retrieve()
					.body(String.class);

			JsonNode root = objectMapper.readTree(responseBody);
			String jsonText = root.at("/candidates/0/content/parts/0/text").asString();
			JsonNode extraction = objectMapper.readTree(jsonText);
			return new StructuredExtraction(
					extraction.path("documentType").asString("unknown"),
					extraction.path("confidence").asDouble(0.0),
					extraction.path("extractedFields").toString());
		} catch (Exception exception) {
			throw new GeminiExtractionException("Gemini extraction failed", exception);
		}
	}
}
