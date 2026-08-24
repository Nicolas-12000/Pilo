package com.pilo.documents;

public record PresignedUrlResponse(String uploadUrl, String method, String note) {}
