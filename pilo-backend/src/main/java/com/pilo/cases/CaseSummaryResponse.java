package com.pilo.cases;

import java.time.Instant;
import java.util.UUID;

public record CaseSummaryResponse(
		UUID id,
		String caseNumber,
		UUID procedureTypeId,
		String procedureTypeTitle,
		CaseStatus status,
		int progressPercentage,
		Instant deadlineAt,
		Instant createdAt) {

	public static CaseSummaryResponse from(ProcedureCase procedureCase) {
		return new CaseSummaryResponse(
				procedureCase.getId(),
				procedureCase.getCaseNumber(),
				procedureCase.getProcedureType().getId(),
				procedureCase.getProcedureType().getTitle(),
				procedureCase.getStatus(),
				procedureCase.getProgressPercentage(),
				procedureCase.getDeadlineAt(),
				procedureCase.getCreatedAt());
	}
}
