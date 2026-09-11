package com.pilo.integrations;

public record ExternalReferenceResponse(
		String sourceName, String externalId, String title, String url, String snippet) {

	public static ExternalReferenceResponse of(String sourceName, String externalId, String title) {
		return new ExternalReferenceResponse(sourceName, externalId, title, null, null);
	}

	public static ExternalReferenceResponse of(
			String sourceName, String externalId, String title, String url, String snippet) {
		return new ExternalReferenceResponse(sourceName, externalId, title, url, snippet);
	}
}
