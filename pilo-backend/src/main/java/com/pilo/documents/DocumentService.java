package com.pilo.documents;

import com.pilo.audit.AuditService;
import com.pilo.cases.CaseAccessService;
import com.pilo.cases.ProcedureCase;
import com.pilo.procedures.Requirement;
import com.pilo.procedures.RequirementRepository;
import com.pilo.users.Role;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DocumentService {

	private final DocumentRepository documentRepository;
	private final RequirementRepository requirementRepository;
	private final CaseAccessService caseAccessService;
	private final LocalDocumentStorage localDocumentStorage;
	private final DocumentProcessingJob documentProcessingJob;
	private final AuditService auditService;

	public DocumentService(
			DocumentRepository documentRepository,
			RequirementRepository requirementRepository,
			CaseAccessService caseAccessService,
			LocalDocumentStorage localDocumentStorage,
			DocumentProcessingJob documentProcessingJob,
			AuditService auditService) {
		this.documentRepository = documentRepository;
		this.requirementRepository = requirementRepository;
		this.caseAccessService = caseAccessService;
		this.localDocumentStorage = localDocumentStorage;
		this.documentProcessingJob = documentProcessingJob;
		this.auditService = auditService;
	}

	@Transactional(readOnly = true)
	public PresignedUrlResponse createUploadInstructions(UUID caseId, UUID userId, Role role, PresignedUrlRequest request) {
		ProcedureCase procedureCase = caseAccessService.requireAccessibleCase(caseId, userId, role);
		Requirement requirement = requirementRepository
				.findDetailedById(request.requirementId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "REQUIREMENT_NOT_FOUND"));
		if (!requirement.getProcedureType().getId().equals(procedureCase.getProcedureType().getId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "REQUIREMENT_MISMATCH");
		}
		return new PresignedUrlResponse(
				"/api/v1/cases/" + caseId + "/documents/upload",
				"POST",
				"Local demo mode: upload multipart form with fields file and requirementId.");
	}

	@Transactional
	public DocumentResponse upload(UUID caseId, UUID userId, Role role, UUID requirementId, MultipartFile file) {
		ProcedureCase procedureCase = caseAccessService.requireAccessibleCase(caseId, userId, role);
		Requirement requirement = requirementRepository
				.findDetailedById(requirementId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "REQUIREMENT_NOT_FOUND"));
		if (!requirement.getProcedureType().getId().equals(procedureCase.getProcedureType().getId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "REQUIREMENT_MISMATCH");
		}
		if (file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "EMPTY_FILE");
		}

		try {
			String storageKey = localDocumentStorage.store(caseId, file.getOriginalFilename(), file.getBytes());
			Document document = new Document();
			document.setProcedureCase(procedureCase);
			document.setRequirement(requirement);
			document.setFileName(file.getOriginalFilename());
			document.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
			document.setFileSize(file.getSize());
			document.setStorageKey(storageKey);
			document.setStatus(DocumentStatus.UPLOADED);
			documentRepository.save(document);

			auditService.record(
					procedureCase.getUser(),
					"DOCUMENT_UPLOADED",
					"document:" + document.getId(),
					"SUCCESS",
					Map.of("requirementCode", requirement.getCode(), "fileName", document.getFileName()));

			documentProcessingJob.enqueue(document.getId());
			return DocumentResponse.from(document);
		} catch (IOException exception) {
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
}
