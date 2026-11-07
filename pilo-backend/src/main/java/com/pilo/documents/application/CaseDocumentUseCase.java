package com.pilo.documents.application;

import com.pilo.documents.DocumentResponse;
import com.pilo.documents.DocumentService;
import com.pilo.documents.PresignedUrlRequest;
import com.pilo.documents.PresignedUrlResponse;
import com.pilo.users.Role;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CaseDocumentUseCase {

	private final DocumentService documentService;

	public CaseDocumentUseCase(DocumentService documentService) {
		this.documentService = documentService;
	}

	public PresignedUrlResponse requestPresignedUpload(
			UUID caseId, UUID userId, Role role, PresignedUrlRequest request) {
		return documentService.createUploadInstructions(caseId, userId, role, request);
	}

	public DocumentResponse uploadMultipart(
			UUID caseId, UUID userId, Role role, UUID requirementId, MultipartFile file) {
		return documentService.upload(caseId, userId, role, requirementId, file);
	}

	public List<DocumentResponse> listDocuments(UUID caseId, UUID userId, Role role) {
		return documentService.list(caseId, userId, role);
	}

	public void deleteDocument(UUID caseId, UUID documentId, UUID userId, Role role) {
		documentService.delete(caseId, documentId, userId, role);
	}
}
