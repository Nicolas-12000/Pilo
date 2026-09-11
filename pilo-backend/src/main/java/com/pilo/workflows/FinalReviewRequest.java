package com.pilo.workflows;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FinalReviewRequest(@NotNull Decision decision, @Size(max = 500) String comment) {

	public enum Decision {
		APPROVED,
		REJECTED
	}
}
