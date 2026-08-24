package com.pilo.documents;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PresignedUrlRequest(@NotNull UUID requirementId) {}
