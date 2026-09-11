package com.pilo.documents;

import java.time.Instant;
import java.util.Map;

public record DocumentExtractionResponse(
		String documentTypeDetected, double confidence, Map<String, String> fields, Instant processedAt) {}
