package com.pilo.documents;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record PresignedUrlRequest(
		@NotNull UUID requirementId,
		@NotBlank String fileName,
		@NotBlank String mimeType,
		@Positive long fileSize) {}
