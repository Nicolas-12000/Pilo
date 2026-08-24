package com.pilo.documents;

import com.pilo.ai.ExtractionHint;
import com.pilo.ai.GeminiExtractionClient;
import com.pilo.ai.StructuredExtraction;
import com.pilo.audit.AuditService;
import com.pilo.documents.validation.ValidationRulesParser;
import com.pilo.workflows.WorkflowService;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentProcessingService {

	private final DocumentRepository documentRepository;
	private final DocumentExtractionRepository documentExtractionRepository;
	private final LocalDocumentStorage localDocumentStorage;
	private final GeminiExtractionClient geminiExtractionClient;
	private final DocumentValidationService documentValidationService;
	private final ValidationRulesParser validationRulesParser;
	private final WorkflowService workflowService;
	private final AuditService auditService;

	public DocumentProcessingService(
			DocumentRepository documentRepository,
			DocumentExtractionRepository documentExtractionRepository,
			LocalDocumentStorage localDocumentStorage,
			GeminiExtractionClient geminiExtractionClient,
			DocumentValidationService documentValidationService,
			ValidationRulesParser validationRulesParser,
			WorkflowService workflowService,
			AuditService auditService) {
		this.documentRepository = documentRepository;
		this.documentExtractionRepository = documentExtractionRepository;
		this.localDocumentStorage = localDocumentStorage;
		this.geminiExtractionClient = geminiExtractionClient;
		this.documentValidationService = documentValidationService;
		this.validationRulesParser = validationRulesParser;
		this.workflowService = workflowService;
		this.auditService = auditService;
	}

	public void process(UUID documentId) {
		ProcessingContext context = markProcessing(documentId);
		try {
			StructuredExtraction extraction = geminiExtractionClient.extract(
					localDocumentStorage.resolve(context.caseId(), context.storageKey()),
					context.mimeType(),
					new ExtractionHint(context.requirementCode(), context.expectedDocumentType()));
			applyExtraction(documentId, extraction);
		} catch (Exception exception) {
			markFailed(documentId, exception.getMessage());
		}
	}

	@Transactional
	public ProcessingContext markProcessing(UUID documentId) {
		Document document = documentRepository.findForProcessing(documentId).orElseThrow();
		document.setStatus(DocumentStatus.PROCESSING);
		documentRepository.save(document);
		return new ProcessingContext(
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

		boolean valid = documentValidationService.isValid(extraction, document.getRequirement());
		document.setStatus(valid ? DocumentStatus.VALIDATED : DocumentStatus.REJECTED);
		documentRepository.save(document);

		auditService.record(
				document.getProcedureCase().getUser(),
				valid ? "DOCUMENT_VALIDATED" : "DOCUMENT_REJECTED",
				"document:" + document.getId(),
				valid ? "SUCCESS" : "FAILURE",
				Map.of("documentType", extraction.documentType(), "confidence", extraction.confidence()));

		workflowService.reevaluate(document.getProcedureCase().getId());
	}

	public record ProcessingContext(
			UUID caseId,
			String storageKey,
			String mimeType,
			String requirementCode,
			String expectedDocumentType) {}
}
