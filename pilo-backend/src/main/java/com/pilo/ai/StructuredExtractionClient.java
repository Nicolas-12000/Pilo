package com.pilo.ai;

public interface StructuredExtractionClient {

	StructuredExtraction extract(ExtractionInput input);

	record ExtractionInput(byte[] content, String mimeType, ExtractionHint hint) {}
}
