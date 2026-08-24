package com.pilo.documents;

import com.pilo.common.AuthSupport;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/cases/{caseId}/documents")
public class DocumentController {

	private final DocumentService documentService;

	public DocumentController(DocumentService documentService) {
		this.documentService = documentService;
	}

	@PostMapping("/presigned-url")
	public PresignedUrlResponse presignedUrl(
			@PathVariable UUID caseId,
			@Valid @RequestBody PresignedUrlRequest request,
			Authentication authentication) {
		return documentService.createUploadInstructions(
				caseId, AuthSupport.userId(authentication), AuthSupport.role(authentication), request);
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DocumentResponse upload(
			@PathVariable UUID caseId,
			@RequestParam UUID requirementId,
			@RequestParam("file") MultipartFile file,
			Authentication authentication) {
		return documentService.upload(
				caseId, AuthSupport.userId(authentication), AuthSupport.role(authentication), requirementId, file);
	}

	@GetMapping
	public List<DocumentResponse> list(@PathVariable UUID caseId, Authentication authentication) {
		return documentService.list(caseId, AuthSupport.userId(authentication), AuthSupport.role(authentication));
	}
}
