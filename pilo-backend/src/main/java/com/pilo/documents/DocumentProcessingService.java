package com.pilo.documents;

import com.pilo.ai.ExtractionHint;
import com.pilo.ai.StructuredExtraction;
import com.pilo.ai.StructuredExtractionClient;
import com.pilo.audit.AuditService;
import com.pilo.documents.storage.ObjectStorage;
import com.pilo.documents.validation.ValidationRulesParser;
import com.pilo.workflows.WorkflowService;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentProcessingService {

	static final int MAX_ATTEMPTS = 5;
	private static final int INLINE_RETRIES = 3;
	private static final Set<DocumentStatus> STARTABLE =
			EnumSet.of(DocumentStatus.PENDING_UPLOAD, DocumentStatus.UPLOADED, DocumentStatus.PROCESSING_FAILED);
	private static final Set<DocumentStatus> TERMINAL =
			EnumSet.of(DocumentStatus.VALIDATED, DocumentStatus.REJECTED);

	private final DocumentRepository documentRepository;
	private final DocumentExtractionRepository documentExtractionRepository;
	private final ObjectStorage objectStorage;
	private final StructuredExtractionClient structuredExtractionClient;
	private final DocumentValidationService documentValidationService;
	private final ValidationRulesParser validationRulesParser;
	private final WorkflowService workflowService;
	private final AuditService auditService;
	private final DocumentGuidanceService documentGuidanceService;

	public DocumentProcessingService(
			DocumentRepository documentRepository,
			DocumentExtractionRepository documentExtractionRepository,
			ObjectStorage objectStorage,
			StructuredExtractionClient structuredExtractionClient,
			DocumentValidationService documentValidationService,
			ValidationRulesParser validationRulesParser,
			WorkflowService workflowService,
			AuditService auditService,
			DocumentGuidanceService documentGuidanceService) {
		this.documentRepository = documentRepository;
		this.documentExtractionRepository = documentExtractionRepository;
		this.objectStorage = objectStorage;
		this.structuredExtractionClient = structuredExtractionClient;
		this.documentValidationService = documentValidationService;
		this.validationRulesParser = validationRulesParser;
		this.workflowService = workflowService;
		this.auditService = auditService;
		this.documentGuidanceService = documentGuidanceService;
	}

	public void process(UUID documentId) {
		Exception lastFailure = null;
		for (int attempt = 1; attempt <= INLINE_RETRIES; attempt++) {
			try {
				ProcessingContext context = markProcessing(documentId);
				if (context.alreadyComplete()) {
					return;
				}
				StructuredExtraction extraction = extract(context);
				applyExtraction(documentId, extraction);
				return;
			} catch (IllegalStateException notReady) {
				return;
			} catch (Exception exception) {
				lastFailure = exception;
			}
		}
		markFailed(documentId, lastFailure == null ? "unknown" : lastFailure.getMessage());
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
		if (TERMINAL.contains(document.getStatus())
				|| (document.getStatus() == DocumentStatus.PROCESSING_FAILED
						&& document.getProcessingAttempts() >= MAX_ATTEMPTS)) {
			return toContext(document, true);
		}
		if (document.getStatus() == DocumentStatus.PROCESSING) {
			return toContext(document, false);
		}
		if (!STARTABLE.contains(document.getStatus())) {
			throw new IllegalStateException("DOCUMENT_NOT_READY_FOR_PROCESSING");
		}
		document.setStatus(DocumentStatus.PROCESSING);
		document.setProcessingAttempts(document.getProcessingAttempts() + 1);
		document.setLastError(null);
		documentRepository.save(document);
		return toContext(document, false);
	}

	@Transactional
	public void markFailed(UUID documentId, String reason) {
		Document document = documentRepository.findForProcessing(documentId).orElseThrow();
		if (TERMINAL.contains(document.getStatus())) {
			return;
		}
		document.setStatus(DocumentStatus.PROCESSING_FAILED);
		document.setLastError(truncate(reason));
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
		if (TERMINAL.contains(document.getStatus())
				|| documentExtractionRepository.existsByDocument_Id(document.getId())) {
			return;
		}

		var validation = documentValidationService.validate(extraction, document.getRequirement());
		var guidance = documentGuidanceService.build(document.getRequirement(), extraction, validation);

		try {
			DocumentExtraction stored = new DocumentExtraction();
			stored.setDocument(document);
			stored.setDocumentTypeDetected(extraction.documentType());
			stored.setConfidence(extraction.confidence());
			stored.setExtractedData(extraction.extractedFieldsJson());
			stored.setCitizenGuidance(guidance.citizenGuidance());
			stored.setReviewerSummary(guidance.reviewerSummary());
			documentExtractionRepository.save(stored);
		} catch (DataIntegrityViolationException duplicate) {
			return;
		}
		document.setStatus(validation.valid() ? DocumentStatus.VALIDATED : DocumentStatus.REJECTED);
		document.setLastError(null);
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

	private ProcessingContext toContext(Document document, boolean alreadyComplete) {
		return new ProcessingContext(
				document.getId(),
				document.getProcedureCase().getId(),
				document.getStorageKey(),
				document.getMimeType(),
				document.getRequirement().getCode(),
				validationRulesParser.parse(document.getRequirement().getValidationRules()).expectedDocumentType(),
				alreadyComplete);
	}

	private static String truncate(String reason) {
		if (reason == null) {
			return "unknown";
		}
		return reason.length() <= 512 ? reason : reason.substring(0, 512);
	}

	public record ProcessingContext(
			UUID documentId,
			UUID caseId,
			String storageKey,
			String mimeType,
			String requirementCode,
			String expectedDocumentType,
			boolean alreadyComplete) {}
}
