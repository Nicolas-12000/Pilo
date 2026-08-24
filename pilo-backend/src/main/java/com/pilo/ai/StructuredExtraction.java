package com.pilo.ai;

public record StructuredExtraction(String documentType, double confidence, String extractedFieldsJson) {}
