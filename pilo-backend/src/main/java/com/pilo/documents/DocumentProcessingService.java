package com.pilo.documents;

import com.pilo.ai.ExtractionHint;
import com.pilo.ai.StructuredExtraction;
import com.pilo.ai.StructuredExtractionClient;
import com.pilo.audit.AuditService;
import com.pilo.documents.storage.ObjectStorage;
import com.pilo.documents.validation.ValidationRulesParser;
import com.pilo.workflows.WorkflowService;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentProcessingService {

	private final DocumentRepository documentRepository;
	private final DocumentExtractionRepository documentExtractionRepository;
	private final ObjectStorage objectStorage;
	private final StructuredExtractionClient structuredExtractionClient;
	private final DocumentValidationService documentValidationService;
	private final ValidationRulesParser validationRulesParser;
	private final WorkflowService workflowService;
	private final AuditService auditService;

	public DocumentProcessingService(
			DocumentRepository documentRepository,
			DocumentExtractionRepository documentExtractionRepository,
			ObjectStorage objectStorage,
			StructuredExtractionClient structuredExtractionClient,
			DocumentValidationService documentValidationService,
			ValidationRulesParser validationRulesParser,
			WorkflowService workflowService,
			AuditService auditService) {
		this.documentRepository = documentRepository;
		this.documentExtractionRepository = documentExtractionRepository;
		this.objectStorage = objectStorage;
		this.structuredExtractionClient = structuredExtractionClient;
		this.documentValidationService = documentValidationService;
		this.validationRulesParser = validationRulesParser;
		this.workflowService = workflowService;
		this.auditService = auditService;
	}

	public void process(UUID documentId) {
		ProcessingContext context = markProcessing(documentId);
		try {
			StructuredExtraction extraction = extract(context);
			applyExtraction(documentId, extraction);
		} catch (Exception exception) {
			markFailed(documentId, exception.getMessage());
		}
	}

	private StructuredExtraction extract(ProcessingContext context) throws IOException {
		try (var stored = objectStorage.open(context.storageKey())) {
			byte[] content = stored.content().readAllBytes();
			return structuredExtractionClient.extract(new StructuredExtractionClient.ExtractionInput(
					content,
					context.mimeType(),
					new ExtractionHint(context.requirementCode(), context.expectedDocumentType())));
		}
	}

	@Transactional
	public ProcessingContext markProcessing(UUID documentId) {
		Document document = documentRepository.findForProcessing(documentId).orElseThrow();
		if (document.getStatus() != DocumentStatus.UPLOADED && document.getStatus() != DocumentStatus.PENDING_UPLOAD) {
			throw new IllegalStateException("DOCUMENT_NOT_READY_FOR_PROCESSING");
		}
		document.setStatus(DocumentStatus.PROCESSING);
		documentRepository.save(document);
		return new ProcessingContext(
				document.getId(),
				document.getProcedureCase().getId(),
				document.getStorageKey(),
				document.getMimeType(),
				document.getRequirement().getCode(),
				validationRulesParser.parse(document.getRequirement().getValidationRules()).expectedDocumentType());
	}

	@Transactional
	public void markFailed(UUID documentId, String reason) {
		Document document = documentRepository.findForProcessing(documentId).orElseThrow();
		document.setStatus(DocumentStatus.PROCESSING_FAILED);
		documentRepository.save(document);
		auditService.record(
				document.getProcedureCase().getUser(),
				"DOCUMENT_PROCESSING_FAILED",
				"document:" + document.getId(),
				"FAILURE",
				Map.of("reason", reason == null ? "unknown" : reason));
	}

	@Transactional
	public void applyExtraction(UUID documentId, StructuredExtraction extraction) {
		Document document = documentRepository.findForProcessing(documentId).orElseThrow();
		applyExtraction(document, extraction);
	}

	@Transactional
	public void applyExtraction(Document document, StructuredExtraction extraction) {
		DocumentExtraction stored = new DocumentExtraction();
		stored.setDocument(document);
		stored.setDocumentTypeDetected(extraction.documentType());
		stored.setConfidence(extraction.confidence());
		stored.setExtractedData(extraction.extractedFieldsJson());
		documentExtractionRepository.save(stored);

		var validation = documentValidationService.validate(extraction, document.getRequirement());
		document.setStatus(validation.valid() ? DocumentStatus.VALIDATED : DocumentStatus.REJECTED);
		documentRepository.save(document);

		Map<String, Object> metadata = new java.util.HashMap<>();
		metadata.put("documentType", extraction.documentType());
		metadata.put("confidence", extraction.confidence());
		if (!validation.valid()) {
			metadata.put("failures", validation.failures());
		}

		auditService.record(
				document.getProcedureCase().getUser(),
				validation.valid() ? "DOCUMENT_VALIDATED" : "DOCUMENT_REJECTED",
				"document:" + document.getId(),
				validation.valid() ? "SUCCESS" : "FAILURE",
				metadata);

		workflowService.reevaluate(document.getProcedureCase().getId());
	}

	public record ProcessingContext(
			UUID documentId,
			UUID caseId,
			String storageKey,
			String mimeType,
			String requirementCode,
			String expectedDocumentType) {}
}
