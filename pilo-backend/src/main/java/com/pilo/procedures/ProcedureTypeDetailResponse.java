package com.pilo.procedures;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record ProcedureTypeDetailResponse(
		UUID id,
		String title,
		String description,
		int targetDays,
		List<RequirementResponse> requirements) {

	public static ProcedureTypeDetailResponse from(ProcedureType procedureType) {
		List<RequirementResponse> requirements = procedureType.getRequirements().stream()
				.sorted(Comparator.comparing(Requirement::getCode))
				.map(RequirementResponse::from)
				.toList();
		return new ProcedureTypeDetailResponse(
				procedureType.getId(),
				procedureType.getTitle(),
				procedureType.getDescription(),
				procedureType.getTargetDays(),
				requirements);
	}
}
