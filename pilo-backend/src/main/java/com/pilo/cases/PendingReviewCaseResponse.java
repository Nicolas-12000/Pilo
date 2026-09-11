package com.pilo.cases;

import java.time.Instant;
import java.util.UUID;

public record PendingReviewCaseResponse(
		UUID id,
		String caseNumber,
		UUID procedureTypeId,
		String procedureTypeTitle,
		CaseStatus status,
		int progressPercentage,
		Instant deadlineAt,
		Instant createdAt,
		String applicantFullName,
		String applicantEmail) {

	public static PendingReviewCaseResponse from(ProcedureCase procedureCase) {
		return new PendingReviewCaseResponse(
				procedureCase.getId(),
				procedureCase.getCaseNumber(),
				procedureCase.getProcedureType().getId(),
				procedureCase.getProcedureType().getTitle(),
				procedureCase.getStatus(),
				procedureCase.getProgressPercentage(),
				procedureCase.getDeadlineAt(),
				procedureCase.getCreatedAt(),
				procedureCase.getUser().getFullName(),
				procedureCase.getUser().getEmail());
	}
}
