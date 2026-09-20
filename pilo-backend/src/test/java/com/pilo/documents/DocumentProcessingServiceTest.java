package com.pilo.documents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pilo.ai.StructuredExtraction;
import com.pilo.ai.StructuredExtractionClient;
import com.pilo.audit.AuditService;
import com.pilo.cases.ProcedureCase;
import com.pilo.documents.storage.ObjectStorage;
import com.pilo.documents.validation.ValidationResult;
import com.pilo.documents.validation.ValidationRules;
import com.pilo.documents.validation.ValidationRulesParser;
import com.pilo.procedures.Requirement;
import com.pilo.users.User;
import com.pilo.workflows.WorkflowService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentProcessingServiceTest {

	@Mock
	private DocumentRepository documentRepository;

	@Mock
	private DocumentExtractionRepository documentExtractionRepository;

	@Mock
	private ObjectStorage objectStorage;

	@Mock
	private StructuredExtractionClient structuredExtractionClient;

	@Mock
	private DocumentValidationService documentValidationService;

	@Mock
	private ValidationRulesParser validationRulesParser;

	@Mock
	private WorkflowService workflowService;

	@Mock
	private AuditService auditService;

	private DocumentProcessingService service;
	private final UUID documentId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		service = new DocumentProcessingService(
				documentRepository,
				documentExtractionRepository,
				objectStorage,
				structuredExtractionClient,
				documentValidationService,
				validationRulesParser,
				workflowService,
				auditService);
	}

	@Test
	void markProcessingIsNoOpWhenAlreadyValidated() {
		Document document = document(DocumentStatus.VALIDATED);
		when(documentRepository.findForProcessing(documentId)).thenReturn(Optional.of(document));
		when(validationRulesParser.parse(any())).thenReturn(new ValidationRules("identity_document", false, null));

		var context = service.markProcessing(documentId);

		assertThat(context.alreadyComplete()).isTrue();
		verify(documentRepository, never()).save(any());
	}

	@Test
	void applyExtractionIgnoresDuplicateCallback() {
		Document document = document(DocumentStatus.VALIDATED);
		when(documentRepository.findForProcessing(documentId)).thenReturn(Optional.of(document));

		service.applyExtraction(
				documentId, new StructuredExtraction("identity_document", 0.9, "{\"name\":\"Ana\"}"));

		verify(documentExtractionRepository, never()).save(any());
		verify(workflowService, never()).reevaluate(any());
	}

	@Test
	void applyExtractionIsIdempotentWhenExtractionRowExists() {
		Document document = document(DocumentStatus.PROCESSING);
		when(documentRepository.findForProcessing(documentId)).thenReturn(Optional.of(document));
		when(documentExtractionRepository.existsByDocument_Id(documentId)).thenReturn(true);

		service.applyExtraction(
				documentId, new StructuredExtraction("identity_document", 0.9, "{\"name\":\"Ana\"}"));

		verify(documentExtractionRepository, never()).save(any());
	}

	@Test
	void applyExtractionValidatesOnce() {
		Document document = document(DocumentStatus.PROCESSING);
		when(documentRepository.findForProcessing(documentId)).thenReturn(Optional.of(document));
		when(documentExtractionRepository.existsByDocument_Id(documentId)).thenReturn(false);
		when(documentValidationService.validate(any(), any())).thenReturn(new ValidationResult(true, List.of()));

		service.applyExtraction(
				documentId, new StructuredExtraction("identity_document", 0.9, "{\"name\":\"Ana\"}"));

		assertThat(document.getStatus()).isEqualTo(DocumentStatus.VALIDATED);
		verify(documentExtractionRepository).save(any());
		verify(workflowService).reevaluate(document.getProcedureCase().getId());
	}

	private Document document(DocumentStatus status) {
		User owner = new User();
		ProcedureCase procedureCase = new ProcedureCase();
		procedureCase.setUser(owner);
		try {
			var field = ProcedureCase.class.getDeclaredField("id");
			field.setAccessible(true);
			field.set(procedureCase, UUID.randomUUID());
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
		Requirement requirement = new Requirement();
		requirement.setCode("IDENTITY_DOCUMENT");
		requirement.setValidationRules("{\"expectedDocumentType\":\"identity_document\"}");
		Document document = new Document();
		document.setProcedureCase(procedureCase);
		document.setRequirement(requirement);
		document.setFileName("dni.pdf");
		document.setMimeType("application/pdf");
		document.setStorageKey("cases/demo.pdf");
		document.setStatus(status);
		try {
			var field = Document.class.getDeclaredField("id");
			field.setAccessible(true);
			field.set(document, documentId);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
		return document;
	}
}
