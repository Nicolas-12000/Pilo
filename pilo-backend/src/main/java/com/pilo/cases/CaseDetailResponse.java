package com.pilo.cases;

import com.pilo.audit.AuditEventResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CaseDetailResponse(
		UUID id,
		String caseNumber,
		UUID procedureTypeId,
		String procedureTypeTitle,
		String procedureTypeDescription,
		CaseStatus status,
		int progressPercentage,
		Instant deadlineAt,
		Instant createdAt,
		List<CaseRequirementStatusResponse> requirements,
		List<AuditEventResponse> recentAuditEvents) {}
