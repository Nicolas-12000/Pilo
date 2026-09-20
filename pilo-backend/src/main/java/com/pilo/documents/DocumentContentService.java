package com.pilo.documents;

import com.pilo.audit.AuditService;
import com.pilo.cases.CaseAccessService;
import com.pilo.documents.storage.ObjectStorage;
import com.pilo.documents.storage.StorageException;
import com.pilo.users.Role;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DocumentContentService {

	private static final Set<DocumentStatus> READABLE_STATUSES = Set.of(
			DocumentStatus.UPLOADED,
			DocumentStatus.PROCESSING,
			DocumentStatus.VALIDATED,
			DocumentStatus.REJECTED,
			DocumentStatus.PROCESSING_FAILED);

	private final DocumentRepository documentRepository;
	private final CaseAccessService caseAccessService;
	private final ObjectStorage objectStorage;
	private final AuditService auditService;

	public DocumentContentService(
			DocumentRepository documentRepository,
			CaseAccessService caseAccessService,
			ObjectStorage objectStorage,
			AuditService auditService) {
		this.documentRepository = documentRepository;
		this.caseAccessService = caseAccessService;
		this.objectStorage = objectStorage;
		this.auditService = auditService;
	}

	@Transactional(readOnly = true)
	public ResponseEntity<byte[]> stream(
			UUID caseId, UUID documentId, UUID userId, Role role, boolean download) {
		var procedureCase = caseAccessService.requireAccessibleCase(caseId, userId, role);
		Document document = documentRepository
				.findByIdAndProcedureCaseId(documentId, caseId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DOCUMENT_NOT_FOUND"));

		if (!READABLE_STATUSES.contains(document.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "DOCUMENT_NOT_READY");
		}

		byte[] bytes;
		try (var stored = objectStorage.open(document.getStorageKey())) {
			bytes = stored.content().readAllBytes();
		} catch (StorageException exception) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DOCUMENT_NOT_FOUND", exception);
		} catch (Exception exception) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR", exception);
		}

		if (download) {
			auditService.record(
					procedureCase.getUser(),
					"DOCUMENT_DOWNLOADED",
					"document:" + document.getId(),
					"SUCCESS",
					Map.of("fileName", document.getFileName(), "actorRole", role.name()));
		}

		MediaType mediaType = parseMediaType(document.getMimeType());
		ContentDisposition disposition = ContentDisposition.builder(download ? "attachment" : "inline")
				.filename(sanitizeFileName(document.getFileName()), StandardCharsets.UTF_8)
				.build();

		return ResponseEntity.ok()
				.contentType(mediaType)
				.contentLength(bytes.length)
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
				.header(HttpHeaders.CACHE_CONTROL, "private, no-store")
				.header("X-Content-Type-Options", "nosniff")
				.body(bytes);
	}

	static String sanitizeFileName(String fileName) {
		if (fileName == null || fileName.isBlank()) {
			return "document";
		}
		String base = fileName.replace("\\", "/");
		int slash = base.lastIndexOf('/');
		if (slash >= 0) {
			base = base.substring(slash + 1);
		}
		base = base.replaceAll("[\\r\\n\"]", "_").trim();
		return base.isBlank() ? "document" : base;
	}

	private static MediaType parseMediaType(String mimeType) {
		try {
			return MediaType.parseMediaType(mimeType);
		} catch (Exception exception) {
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}
}
