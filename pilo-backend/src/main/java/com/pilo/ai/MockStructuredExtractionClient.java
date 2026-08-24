package com.pilo.ai;

public class MockStructuredExtractionClient implements StructuredExtractionClient {

	@Override
	public StructuredExtraction extract(ExtractionInput input) {
		String documentType = input.hint().expectedDocumentType() == null
						|| input.hint().expectedDocumentType().isBlank()
				? "unknown_document"
				: input.hint().expectedDocumentType();
		String fields =
				"""
				{"personName":"Ana Usuario","idNumber":"12345678X","issueDate":"2026-01-01","expirationDate":"2027-12-31","address":"Calle Mayor 12, Madrid"}
				""";
		return new StructuredExtraction(documentType, 0.95, fields);
	}
}
