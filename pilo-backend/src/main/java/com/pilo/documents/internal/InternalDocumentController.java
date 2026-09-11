package com.pilo.documents.internal;

import com.pilo.documents.Document;
import com.pilo.documents.DocumentProcessingService;
import com.pilo.documents.DocumentRepository;
import com.pilo.documents.DocumentStatus;
import com.pilo.documents.StorageProperties;
import com.pilo.documents.validation.ValidationRulesParser;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/documents/{documentId}")
public class InternalDocumentController {

	private final DocumentRepository documentRepository;
	private final DocumentProcessingService documentProcessingService;
	private final ValidationRulesParser validationRulesParser;
	private final StorageProperties storageProperties;

	public InternalDocumentController(
			DocumentRepository documentRepository,
			DocumentProcessingService documentProcessingService,
			ValidationRulesParser validationRulesParser,
			StorageProperties storageProperties) {
		this.documentRepository = documentRepository;
		this.documentProcessingService = documentProcessingService;
		this.validationRulesParser = validationRulesParser;
		this.storageProperties = storageProperties;
	}

	@PostMapping("/upload-completed")
	@Transactional
	public void uploadCompleted(@PathVariable UUID documentId) {
		Document document = documentRepository.findForProcessing(documentId).orElseThrow();
		if (document.getStatus() != DocumentStatus.PENDING_UPLOAD) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "INVALID_DOCUMENT_STATUS");
		}
		document.setStatus(DocumentStatus.UPLOADED);
		documentRepository.save(document);
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
				rules.expectedDocumentType());
	}

	public record ProcessingContextResponse(
			UUID documentId,
			UUID caseId,
			String storageKey,
			String bucket,
			String storageProvider,
			String mimeType,
			String requirementCode,
			String expectedDocumentType) {

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
					context.expectedDocumentType());
		}
	}
}
