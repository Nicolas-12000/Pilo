package com.pilo.ai;

import tools.jackson.databind.ObjectMapper;
import com.pilo.documents.Document;
import com.pilo.documents.DocumentProcessingService;
import com.pilo.documents.DocumentRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/documents")
public class InternalExtractionController {

	private final DocumentRepository documentRepository;
	private final DocumentProcessingService documentProcessingService;
	private final ObjectMapper objectMapper;

	public InternalExtractionController(
			DocumentRepository documentRepository,
			DocumentProcessingService documentProcessingService,
			ObjectMapper objectMapper) {
		this.documentRepository = documentRepository;
		this.documentProcessingService = documentProcessingService;
		this.objectMapper = objectMapper;
	}

	@PostMapping("/{documentId}/extractions")
	public void receiveExtraction(
			@PathVariable UUID documentId, @Valid @RequestBody ExtractionCallbackRequest request)
			throws Exception {
		Document document = documentRepository.findForProcessing(documentId).orElseThrow();
		String extractedFieldsJson = objectMapper.writeValueAsString(request.extractedFields());
		documentProcessingService.applyExtraction(
				document, new StructuredExtraction(request.documentType(), request.confidence(), extractedFieldsJson));
	}

	public record ExtractionCallbackRequest(
			@NotBlank String documentType,
			@NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double confidence,
			@NotNull Map<String, Object> extractedFields) {}
}
