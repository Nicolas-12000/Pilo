package com.pilo.procedures;

import java.util.UUID;

public record ProcedureTypeSummaryResponse(
		UUID id, String title, String description, int targetDays, int requirementCount) {

	public static ProcedureTypeSummaryResponse from(ProcedureType procedureType) {
		return new ProcedureTypeSummaryResponse(
				procedureType.getId(),
				procedureType.getTitle(),
				procedureType.getDescription(),
				procedureType.getTargetDays(),
				procedureType.getRequirements().size());
	}
}
