package com.pilo.documents;

import com.pilo.audit.AuditService;
import com.pilo.cases.CaseAccessService;
import com.pilo.cases.ProcedureCase;
import com.pilo.documents.storage.ObjectStorage;
import com.pilo.procedures.Requirement;
import com.pilo.procedures.RequirementRepository;
import com.pilo.users.Role;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DocumentService {

	private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
			"application/pdf", "image/jpeg", "image/png", "image/webp");
	private static final long MAX_FILE_SIZE = 15L * 1024 * 1024;

	private final DocumentRepository documentRepository;
	private final RequirementRepository requirementRepository;
	private final CaseAccessService caseAccessService;
	private final ObjectStorage objectStorage;
	private final DocumentProcessingJob documentProcessingJob;
	private final ProcessingProperties processingProperties;
	private final AuditService auditService;

	public DocumentService(
			DocumentRepository documentRepository,
			RequirementRepository requirementRepository,
			CaseAccessService caseAccessService,
			ObjectStorage objectStorage,
			DocumentProcessingJob documentProcessingJob,
			ProcessingProperties processingProperties,
			AuditService auditService) {
		this.documentRepository = documentRepository;
		this.requirementRepository = requirementRepository;
		this.caseAccessService = caseAccessService;
		this.objectStorage = objectStorage;
		this.documentProcessingJob = documentProcessingJob;
		this.processingProperties = processingProperties;
		this.auditService = auditService;
	}

	@Transactional
	public PresignedUrlResponse createUploadInstructions(
			UUID caseId, UUID userId, Role role, PresignedUrlRequest request) {
		RequirementContext context = validateRequirement(caseId, userId, role, request.requirementId());
		validateUploadMetadata(request.fileName(), request.mimeType(), request.fileSize());

		if (!"s3".equals(objectStorage.provider())) {
			return new PresignedUrlResponse(
					"/api/v1/cases/" + caseId + "/documents/upload",
					"POST",
					null,
					Map.of(),
					"Local demo mode: upload multipart form with fields file and requirementId.");
		}

		Document document = createDocumentRecord(
				context.procedureCase(),
				context.requirement(),
				request.fileName(),
				request.mimeType(),
				request.fileSize(),
				DocumentStatus.PENDING_UPLOAD,
				null);

		var presigned = objectStorage.presignUpload(new ObjectStorage.UploadInstruction(
				caseId, document.getId(), request.fileName(), request.mimeType(), request.fileSize()));

		document.setStorageKey(presigned.storageKey());
		documentRepository.save(document);

		auditService.record(
				context.procedureCase().getUser(),
				"DOCUMENT_UPLOAD_REQUESTED",
				"document:" + document.getId(),
				"SUCCESS",
				Map.of("requirementCode", context.requirement().getCode(), "fileName", document.getFileName()));

		return new PresignedUrlResponse(
				presigned.uploadUrl(),
				presigned.method(),
				document.getId().toString(),
				presigned.headers(),
				"Upload the file with HTTP PUT, then wait for asynchronous processing.");
	}

	@Transactional
	public DocumentResponse upload(UUID caseId, UUID userId, Role role, UUID requirementId, MultipartFile file) {
		if ("s3".equals(objectStorage.provider())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "USE_PRESIGNED_UPLOAD");
		}

		RequirementContext context = validateRequirement(caseId, userId, role, requirementId);
		if (file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "EMPTY_FILE");
		}
		String mimeType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
		validateUploadMetadata(file.getOriginalFilename(), mimeType, file.getSize());

		Document document = createDocumentRecord(
				context.procedureCase(),
				context.requirement(),
				file.getOriginalFilename(),
				mimeType,
				file.getSize(),
				DocumentStatus.UPLOADED,
				null);

		try {
			var stored = objectStorage.storeInline(new ObjectStorage.InlineUpload(
					caseId,
					document.getId(),
					file.getOriginalFilename(),
					mimeType,
					file.getBytes()));
			document.setStorageKey(stored.storageKey());
			documentRepository.save(document);

			recordUploaded(context, document);
			enqueueInlineProcessing(document.getId());
			return DocumentResponse.from(document);
		} catch (Exception exception) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR");
		}
	}

	@Transactional(readOnly = true)
	public List<DocumentResponse> list(UUID caseId, UUID userId, Role role) {
		caseAccessService.requireAccessibleCase(caseId, userId, role);
		return documentRepository.findByCaseId(caseId).stream()
				.map(DocumentResponse::from)
				.toList();
	}

	private RequirementContext validateRequirement(UUID caseId, UUID userId, Role role, UUID requirementId) {
		ProcedureCase procedureCase = caseAccessService.requireAccessibleCase(caseId, userId, role);
		Requirement requirement = requirementRepository
				.findDetailedById(requirementId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "REQUIREMENT_NOT_FOUND"));
		if (!requirement.getProcedureType().getId().equals(procedureCase.getProcedureType().getId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "REQUIREMENT_MISMATCH");
		}
		return new RequirementContext(procedureCase, requirement);
	}

	private Document createDocumentRecord(
			ProcedureCase procedureCase,
			Requirement requirement,
			String fileName,
			String mimeType,
			long fileSize,
			DocumentStatus status,
			String storageKey) {
		Document document = new Document();
		document.setProcedureCase(procedureCase);
		document.setRequirement(requirement);
		document.setFileName(fileName);
		document.setMimeType(mimeType);
		document.setFileSize(fileSize);
		document.setStorageKey(storageKey != null ? storageKey : "pending");
		document.setStatus(status);
		return documentRepository.save(document);
	}

	private void validateUploadMetadata(String fileName, String mimeType, long fileSize) {
		if (fileName == null || fileName.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_FILE_NAME");
		}
		if (fileSize <= 0 || fileSize > MAX_FILE_SIZE) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_FILE_SIZE");
		}
		if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_MIME_TYPE");
		}
	}

	private void recordUploaded(RequirementContext context, Document document) {
		auditService.record(
				context.procedureCase().getUser(),
				"DOCUMENT_UPLOADED",
				"document:" + document.getId(),
				"SUCCESS",
				Map.of("requirementCode", context.requirement().getCode(), "fileName", document.getFileName()));
	}

	private void enqueueInlineProcessing(UUID documentId) {
		if (processingProperties.inline()) {
			documentProcessingJob.enqueue(documentId);
		}
	}

	private record RequirementContext(ProcedureCase procedureCase, Requirement requirement) {}
}
