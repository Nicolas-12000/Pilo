package com.pilo.documents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pilo.audit.AuditService;
import com.pilo.cases.CaseAccessService;
import com.pilo.cases.ProcedureCase;
import com.pilo.documents.storage.ObjectStorage;
import com.pilo.users.Role;
import com.pilo.users.User;
import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class DocumentContentServiceTest {

	@Mock
	private DocumentRepository documentRepository;

	@Mock
	private CaseAccessService caseAccessService;

	@Mock
	private ObjectStorage objectStorage;

	@Mock
	private AuditService auditService;

	private DocumentContentService documentContentService;
	private final UUID caseId = UUID.randomUUID();
	private final UUID documentId = UUID.randomUUID();
	private final UUID userId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		documentContentService =
				new DocumentContentService(documentRepository, caseAccessService, objectStorage, auditService);
	}

	@Test
	void streamsInlinePdfForReviewer() {
		Document document = document("contrato.pdf", DocumentStatus.VALIDATED);
		when(caseAccessService.requireAccessibleCase(caseId, userId, Role.REVIEWER)).thenReturn(document.getProcedureCase());
		when(documentRepository.findByIdAndProcedureCaseId(documentId, caseId)).thenReturn(Optional.of(document));
		when(objectStorage.open("cases/demo.pdf"))
				.thenReturn(new ObjectStorage.StoredContent(
						"cases/demo.pdf", new ByteArrayInputStream("%PDF-1.4".getBytes()), "application/pdf"));

		var response = documentContentService.stream(caseId, documentId, userId, Role.REVIEWER, false);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("inline");
		assertThat(response.getBody()).isEqualTo("%PDF-1.4".getBytes());
	}

	@Test
	void auditsDownloadOnly() {
		Document document = document("dni.png", DocumentStatus.REJECTED);
		when(caseAccessService.requireAccessibleCase(caseId, userId, Role.USER)).thenReturn(document.getProcedureCase());
		when(documentRepository.findByIdAndProcedureCaseId(documentId, caseId)).thenReturn(Optional.of(document));
		when(objectStorage.open("cases/demo.pdf"))
				.thenReturn(new ObjectStorage.StoredContent(
						"cases/demo.pdf", new ByteArrayInputStream(new byte[] {1, 2, 3}), "image/png"));

		documentContentService.stream(caseId, documentId, userId, Role.USER, true);

		verify(auditService)
				.record(
						document.getProcedureCase().getUser(),
						"DOCUMENT_DOWNLOADED",
						"document:" + document.getId(),
						"SUCCESS",
						java.util.Map.of("fileName", "dni.png", "actorRole", "USER"));
	}

	@Test
	void rejectsPendingUpload() {
		Document document = document("pending.pdf", DocumentStatus.PENDING_UPLOAD);
		when(caseAccessService.requireAccessibleCase(caseId, userId, Role.USER)).thenReturn(document.getProcedureCase());
		when(documentRepository.findByIdAndProcedureCaseId(documentId, caseId)).thenReturn(Optional.of(document));

		assertThatThrownBy(() -> documentContentService.stream(caseId, documentId, userId, Role.USER, false))
				.isInstanceOf(ResponseStatusException.class)
				.extracting("reason")
				.isEqualTo("DOCUMENT_NOT_READY");
	}

	@Test
	void sanitizesPathAndNewlinesFromFileName() {
		assertThat(DocumentContentService.sanitizeFileName("../secret\r\n.pdf")).isEqualTo("secret__.pdf");
		assertThat(DocumentContentService.sanitizeFileName("   ")).isEqualTo("document");
	}

	private Document document(String fileName, DocumentStatus status) {
		User owner = new User();
		ProcedureCase procedureCase = new ProcedureCase();
		procedureCase.setUser(owner);
		Document document = new Document();
		document.setProcedureCase(procedureCase);
		document.setFileName(fileName);
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
