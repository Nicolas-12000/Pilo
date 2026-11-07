package com.pilo.documents.internal;

import com.pilo.ai.StructuredExtraction;
import com.pilo.documents.Document;
import com.pilo.documents.DocumentProcessingService;
import com.pilo.documents.DocumentRepository;
import com.pilo.documents.DocumentStatus;
import com.pilo.documents.StorageProperties;
import com.pilo.documents.validation.ValidationRulesParser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

/**
 * Internal HTTP surface for workers (API key). Keeps all {@code /internal/documents/**} routes in one
 * place for clearer module boundaries.
 */
@RestController
@RequestMapping("/internal/documents/{documentId}")
public class InternalDocumentApi {

	private final DocumentRepository documentRepository;
	private final DocumentProcessingService documentProcessingService;
	private final ValidationRulesParser validationRulesParser;
	private final StorageProperties storageProperties;
	private final ObjectMapper objectMapper;

	public InternalDocumentApi(
			DocumentRepository documentRepository,
			DocumentProcessingService documentProcessingService,
			ValidationRulesParser validationRulesParser,
			StorageProperties storageProperties,
			ObjectMapper objectMapper) {
		this.documentRepository = documentRepository;
		this.documentProcessingService = documentProcessingService;
		this.validationRulesParser = validationRulesParser;
		this.storageProperties = storageProperties;
		this.objectMapper = objectMapper;
	}

	@PostMapping("/upload-completed")
	@Transactional
	public void uploadCompleted(@PathVariable UUID documentId) {
		Document document = documentRepository.findForProcessing(documentId).orElseThrow();
		if (document.getStatus() == DocumentStatus.PENDING_UPLOAD) {
			document.setStatus(DocumentStatus.UPLOADED);
			documentRepository.save(document);
		}
	}

	@PostMapping("/processing-started")
	@Transactional
	public ProcessingContextResponse processingStarted(@PathVariable UUID documentId) {
		var context = documentProcessingService.markProcessing(documentId);
		return ProcessingContextResponse.from(context, storageProperties);
	}

	@GetMapping("/processing-context")
	@Transactional(readOnly = true)
	public ProcessingContextResponse processingContext(@PathVariable UUID documentId) {
		Document document = documentRepository.findForProcessing(documentId).orElseThrow();
		var rules = validationRulesParser.parse(document.getRequirement().getValidationRules());
		return new ProcessingContextResponse(
				document.getId(),
				document.getProcedureCase().getId(),
				document.getStorageKey(),
				storageProperties.bucket(),
				storageProperties.provider(),
				document.getMimeType(),
				document.getRequirement().getCode(),
				rules.expectedDocumentType(),
				document.getStatus() == DocumentStatus.VALIDATED
						|| document.getStatus() == DocumentStatus.REJECTED);
	}

	@PostMapping("/extractions")
	public void receiveExtraction(
			@PathVariable UUID documentId, @Valid @RequestBody ExtractionCallbackRequest request)
			throws Exception {
		Document document = documentRepository.findForProcessing(documentId).orElseThrow();
		String extractedFieldsJson = objectMapper.writeValueAsString(request.extractedFields());
		documentProcessingService.applyExtraction(
				document, new StructuredExtraction(request.documentType(), request.confidence(), extractedFieldsJson));
	}

	public record ProcessingContextResponse(
			UUID documentId,
			UUID caseId,
			String storageKey,
			String bucket,
			String storageProvider,
			String mimeType,
			String requirementCode,
			String expectedDocumentType,
			boolean alreadyComplete) {

		static ProcessingContextResponse from(
				DocumentProcessingService.ProcessingContext context, StorageProperties storageProperties) {
			return new ProcessingContextResponse(
					context.documentId(),
					context.caseId(),
					context.storageKey(),
					storageProperties.bucket(),
					storageProperties.provider(),
					context.mimeType(),
					context.requirementCode(),
					context.expectedDocumentType(),
					context.alreadyComplete());
		}
	}

	public record ExtractionCallbackRequest(
			@NotBlank String documentType,
			@NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double confidence,
			@NotNull Map<String, Object> extractedFields) {}
}
