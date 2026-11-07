package com.pilo.documents.application;

import com.pilo.documents.DocumentContentService;
import com.pilo.users.Role;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class StreamDocumentContentUseCase {

	private final DocumentContentService documentContentService;

	public StreamDocumentContentUseCase(DocumentContentService documentContentService) {
		this.documentContentService = documentContentService;
	}

	public ResponseEntity<byte[]> execute(
			UUID caseId, UUID documentId, UUID userId, Role role, boolean download) {
		return documentContentService.stream(caseId, documentId, userId, role, download);
	}
}
