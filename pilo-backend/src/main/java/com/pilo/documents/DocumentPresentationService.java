package com.pilo.documents;

import com.pilo.ai.StructuredExtraction;
import com.pilo.audit.AuditEvent;
import com.pilo.audit.AuditEventRepository;
import com.pilo.documents.validation.ValidationResult;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class DocumentPresentationService {

	private final DocumentExtractionRepository documentExtractionRepository;
	private final DocumentValidationService documentValidationService;
	private final AuditEventRepository auditEventRepository;
	private final ObjectMapper objectMapper;

	public DocumentPresentationService(
			DocumentExtractionRepository documentExtractionRepository,
			DocumentValidationService documentValidationService,
			AuditEventRepository auditEventRepository,
			ObjectMapper objectMapper) {
		this.documentExtractionRepository = documentExtractionRepository;
		this.documentValidationService = documentValidationService;
		this.auditEventRepository = auditEventRepository;
		this.objectMapper = objectMapper;
	}

	public List<DocumentResponse> present(List<Document> documents) {
		if (documents.isEmpty()) {
			return List.of();
		}

		List<UUID> documentIds = documents.stream().map(Document::getId).toList();
		Map<UUID, DocumentExtraction> extractions = documentExtractionRepository.findByDocumentIdIn(documentIds).stream()
				.collect(Collectors.toMap(extraction -> extraction.getDocument().getId(), Function.identity()));
		Map<UUID, String> processingFailures = loadProcessingFailures(documentIds);

		return documents.stream()
				.map(document -> present(document, extractions.get(document.getId()), processingFailures.get(document.getId())))
				.toList();
	}

	private DocumentResponse present(Document document, DocumentExtraction extraction, String processingFailureReason) {
		DocumentExtractionResponse extractionResponse = extraction == null ? null : toExtractionResponse(extraction);
		List<String> validationFailures = List.of();
		if (document.getStatus() == DocumentStatus.REJECTED && extraction != null) {
			StructuredExtraction structured = new StructuredExtraction(
					extraction.getDocumentTypeDetected(),
					extraction.getConfidence(),
					extraction.getExtractedData());
			ValidationResult validation = documentValidationService.validate(structured, document.getRequirement());
			validationFailures = validation.failures();
		}
		return DocumentResponse.from(document, extractionResponse, validationFailures, processingFailureReason);
	}

	private DocumentExtractionResponse toExtractionResponse(DocumentExtraction extraction) {
		return new DocumentExtractionResponse(
				extraction.getDocumentTypeDetected(),
				extraction.getConfidence(),
				parseFields(extraction.getExtractedData()),
				extraction.getProcessedAt());
	}

	private Map<String, String> parseFields(String extractedData) {
		try {
			JsonNode root = objectMapper.readTree(extractedData);
			Map<String, String> fields = new HashMap<>();
			for (String name : root.propertyNames()) {
				String value = root.path(name).asString(null);
				if (value != null && !value.isBlank()) {
					fields.put(name, value);
				}
			}
			return Map.copyOf(fields);
		} catch (Exception exception) {
			return Map.of();
		}
	}

	private Map<UUID, String> loadProcessingFailures(Collection<UUID> documentIds) {
		List<String> resources = documentIds.stream().map(id -> "document:" + id).toList();
		List<AuditEvent> events =
				auditEventRepository.findLatestByResourcesAndAction(resources, "DOCUMENT_PROCESSING_FAILED");
		Map<UUID, String> failures = new HashMap<>();
		for (AuditEvent event : events) {
			UUID documentId = parseDocumentId(event.getResource());
			if (documentId != null && !failures.containsKey(documentId)) {
				failures.put(documentId, extractReason(event.getMetadata()));
			}
		}
		return failures;
	}

	private static UUID parseDocumentId(String resource) {
		if (resource == null || !resource.startsWith("document:")) {
			return null;
		}
		try {
			return UUID.fromString(resource.substring("document:".length()));
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}

	private String extractReason(String metadata) {
		if (metadata == null || metadata.isBlank()) {
			return null;
		}
		try {
			JsonNode root = objectMapper.readTree(metadata);
			return root.path("reason").asString(null);
		} catch (Exception exception) {
			return null;
		}
	}
}
