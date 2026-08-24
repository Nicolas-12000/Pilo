package com.pilo.documents;

import java.util.UUID;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class DocumentProcessingJob {

	private final DocumentProcessingService documentProcessingService;

	public DocumentProcessingJob(DocumentProcessingService documentProcessingService) {
		this.documentProcessingService = documentProcessingService;
	}

	@Async("documentProcessingExecutor")
	public void enqueue(UUID documentId) {
		documentProcessingService.process(documentId);
	}
}
