package com.pilo.integrations;

public record ExternalReferenceResponse(
		String sourceName, String externalId, String title, String url, String snippet, boolean demo) {

	public static ExternalReferenceResponse live(
			String sourceName, String externalId, String title, String url, String snippet) {
		return new ExternalReferenceResponse(sourceName, externalId, title, url, snippet, false);
	}

	public static ExternalReferenceResponse demo(
			String sourceName, String externalId, String title, String url, String snippet) {
		return new ExternalReferenceResponse(sourceName, externalId, title, url, snippet, true);
	}
}
