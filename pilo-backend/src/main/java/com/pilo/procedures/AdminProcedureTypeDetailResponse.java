package com.pilo.procedures;

import com.pilo.documents.validation.ValidationRulesParser;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record AdminProcedureTypeDetailResponse(
		UUID id,
		String title,
		String description,
		int targetDays,
		boolean hasCases,
		List<AdminRequirementResponse> requirements) {

	public static AdminProcedureTypeDetailResponse from(
			ProcedureType procedureType, boolean hasCases, ValidationRulesParser parser) {
		List<AdminRequirementResponse> requirements = procedureType.getRequirements().stream()
				.sorted(Comparator.comparing(Requirement::getCode))
				.map(requirement -> AdminRequirementResponse.from(requirement, parser))
				.toList();
		return new AdminProcedureTypeDetailResponse(
				procedureType.getId(),
				procedureType.getTitle(),
				procedureType.getDescription(),
				procedureType.getTargetDays(),
				hasCases,
				requirements);
	}
}
