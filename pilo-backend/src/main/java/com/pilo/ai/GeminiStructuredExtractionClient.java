package com.pilo.ai;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Mirrors pilo-worker/extraction.py: the model must quote the text behind every field, fields
 * without evidence are dropped, and the reported confidence is scaled by the share kept.
 */
public class GeminiStructuredExtractionClient implements StructuredExtractionClient {

	static final List<String> DOCUMENT_TYPES = List.of(
			"identity_document",
			"proof_of_address",
			"rental_contract",
			"home_insurance",
			"tax_certificate",
			"building_plan",
			"other");
	static final List<String> FIELD_NAMES = List.of("personName", "idNumber", "issueDate", "expirationDate", "address");

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
			// Silently falling back to the mock would mark any upload as valid.
			throw new IllegalStateException("pilo.ai.provider=gemini but GEMINI_API_KEY is not set");
		}

		try {
			String base64 = Base64.getEncoder().encodeToString(input.content());
			Map<String, Object> body = Map.of(
					"contents",
					List.of(Map.of(
							"parts",
							List.of(
									Map.of("inline_data", Map.of("mime_type", input.mimeType(), "data", base64)),
									Map.of("text", prompt(input.hint()))))),
					"generationConfig",
					Map.of(
							"responseMimeType", "application/json",
							"responseJsonSchema", responseSchema(),
							"thinkingConfig", Map.of("thinkingLevel", "low")));

			String responseBody = restClient
					.post()
					.uri("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent"
							.formatted(aiProperties.model()))
					.header("x-goog-api-key", aiProperties.geminiApiKey())
					.contentType(MediaType.APPLICATION_JSON)
					.body(body)
					.retrieve()
					.body(String.class);

			JsonNode root = objectMapper.readTree(responseBody);
			String jsonText = root.at("/candidates/0/content/parts/0/text").asString();
			return normalize(objectMapper.readTree(jsonText));
		} catch (Exception exception) {
			throw new GeminiExtractionException("Gemini extraction failed", exception);
		}
	}

	private static String prompt(ExtractionHint hint) {
		return """
				You are reading a Spanish administrative document uploaded by a citizen.
				Classify it with one of the allowed documentType values; use "other" if none fits.
				For each field, return it only if it is clearly printed in the document, with "evidence" set \
				to the exact text you read it from. If a field is not visible, set it to null. Never infer or complete values.
				Dates must be returned as YYYY-MM-DD. idNumber is a Spanish DNI/NIE without spaces.
				Requirement code: %s. Expected document type: %s.
				"""
				.formatted(hint.requirementCode(), hint.expectedDocumentType());
	}

	private static Map<String, Object> responseSchema() {
		Map<String, Object> field = Map.of(
				"type", List.of("object", "null"),
				"properties", Map.of(
						"value", Map.of("type", "string"),
						"evidence", Map.of(
								"type", "string",
								"description", "Exact text copied from the document where the value appears.")),
				"required", List.of("value", "evidence"));
		Map<String, Object> fields = new LinkedHashMap<>();
		FIELD_NAMES.forEach(name -> fields.put(name, field));
		return Map.of(
				"type", "object",
				"properties", Map.of(
						"documentType", Map.of("type", "string", "enum", DOCUMENT_TYPES),
						"confidence", Map.of("type", "number", "minimum", 0, "maximum", 1),
						"fields", Map.of("type", "object", "properties", fields)),
				"required", List.of("documentType", "confidence", "fields"));
	}

	StructuredExtraction normalize(JsonNode parsed) {
		String documentType = parsed.path("documentType").asString("other");
		if (!DOCUMENT_TYPES.contains(documentType)) {
			documentType = "other";
		}

		JsonNode rawFields = parsed.path("fields");
		int proposed = 0;
		Map<String, String> kept = new LinkedHashMap<>();
		for (String name : FIELD_NAMES) {
			JsonNode field = rawFields.path(name);
			String value = field.path("value").asString("").trim();
			if (value.isEmpty()) {
				continue;
			}
			proposed++;
			if (hasEvidence(name, value, field.path("evidence").asString(""))) {
				kept.put(name, value);
			}
		}

		double modelConfidence = Math.min(Math.max(parsed.path("confidence").asDouble(0.0), 0.0), 1.0);
		double evidenceRatio = proposed == 0 ? 0.0 : (double) kept.size() / proposed;
		double confidence = Math.round(modelConfidence * evidenceRatio * 1000.0) / 1000.0;
		return new StructuredExtraction(documentType, confidence, objectMapper.writeValueAsString(kept));
	}

	private static boolean hasEvidence(String name, String value, String evidence) {
		if (evidence.isBlank()) {
			return false;
		}
		// Identifiers must literally appear in the quoted text; dates and names may be reformatted.
		if ("idNumber".equals(name)) {
			return compact(evidence).contains(compact(value));
		}
		return true;
	}

	private static String compact(String text) {
		return text.toLowerCase(Locale.ROOT).replaceAll("[^0-9a-z]", "");
	}
}
