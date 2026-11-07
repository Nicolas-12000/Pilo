package com.pilo.documents;

import com.pilo.common.AuthSupport;
import com.pilo.documents.application.CaseDocumentUseCase;
import com.pilo.documents.application.StreamDocumentContentUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/cases/{caseId}/documents")
public class DocumentController {

	private final CaseDocumentUseCase caseDocumentUseCase;
	private final StreamDocumentContentUseCase streamDocumentContentUseCase;

	public DocumentController(
			CaseDocumentUseCase caseDocumentUseCase, StreamDocumentContentUseCase streamDocumentContentUseCase) {
		this.caseDocumentUseCase = caseDocumentUseCase;
		this.streamDocumentContentUseCase = streamDocumentContentUseCase;
	}

	@PostMapping("/presigned-url")
	public PresignedUrlResponse presignedUrl(
			@PathVariable UUID caseId,
			@Valid @RequestBody PresignedUrlRequest request,
			Authentication authentication) {
		return caseDocumentUseCase.requestPresignedUpload(
				caseId, AuthSupport.userId(authentication), AuthSupport.role(authentication), request);
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DocumentResponse upload(
			@PathVariable UUID caseId,
			@RequestParam UUID requirementId,
			@RequestParam("file") MultipartFile file,
			Authentication authentication) {
		return caseDocumentUseCase.uploadMultipart(
				caseId, AuthSupport.userId(authentication), AuthSupport.role(authentication), requirementId, file);
	}

	@GetMapping
	public List<DocumentResponse> list(@PathVariable UUID caseId, Authentication authentication) {
		return caseDocumentUseCase.listDocuments(caseId, AuthSupport.userId(authentication), AuthSupport.role(authentication));
	}

	@DeleteMapping("/{documentId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(
			@PathVariable UUID caseId,
			@PathVariable UUID documentId,
			Authentication authentication) {
		caseDocumentUseCase.deleteDocument(
				caseId, documentId, AuthSupport.userId(authentication), AuthSupport.role(authentication));
	}

	@GetMapping("/{documentId}/content")
	public ResponseEntity<byte[]> content(
			@PathVariable UUID caseId,
			@PathVariable UUID documentId,
			@RequestParam(defaultValue = "false") boolean download,
			Authentication authentication) {
		return streamDocumentContentUseCase.execute(
				caseId,
				documentId,
				AuthSupport.userId(authentication),
				AuthSupport.role(authentication),
				download);
	}
}
