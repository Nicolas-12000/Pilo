package com.pilo.documents;

import java.util.Map;

public record PresignedUrlResponse(
		String uploadUrl, String method, String documentId, Map<String, String> headers, String note) {}
