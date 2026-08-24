package com.pilo.ai;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class GeminiExtractionClient {

	private final AiProperties aiProperties;
	private final ObjectMapper objectMapper;
	private final RestClient restClient;

	public GeminiExtractionClient(AiProperties aiProperties, ObjectMapper objectMapper) {
		this.aiProperties = aiProperties;
		this.objectMapper = objectMapper;
		this.restClient = RestClient.builder().build();
	}

	public StructuredExtraction extract(Path filePath, String mimeType, ExtractionHint hint) {
		if (aiProperties.geminiApiKey() == null || aiProperties.geminiApiKey().isBlank()) {
			return mockExtraction(hint);
		}

		try {
			byte[] bytes = Files.readAllBytes(filePath);
			String base64 = Base64.getEncoder().encodeToString(bytes);
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
													.formatted(hint.requirementCode(), hint.expectedDocumentType())),
									Map.of("inline_data", Map.of("mime_type", mimeType, "data", base64))))),
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

	private StructuredExtraction mockExtraction(ExtractionHint hint) {
		String documentType = hint.expectedDocumentType() == null || hint.expectedDocumentType().isBlank()
				? "unknown_document"
				: hint.expectedDocumentType();
		String fields =
				"""
				{"personName":"Ana Usuario","idNumber":"12345678X","issueDate":"2026-01-01","expirationDate":"2027-12-31","address":"Calle Mayor 12, Madrid"}
				""";
		return new StructuredExtraction(documentType, 0.95, fields);
	}
}
