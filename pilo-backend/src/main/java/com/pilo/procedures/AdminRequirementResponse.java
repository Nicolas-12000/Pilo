package com.pilo.procedures;

import com.pilo.documents.validation.ValidationRules;
import com.pilo.documents.validation.ValidationRulesParser;
import java.util.UUID;

public record AdminRequirementResponse(
		UUID id,
		String code,
		String name,
		String description,
		boolean mandatory,
		ValidationRules validationRules) {

	public static AdminRequirementResponse from(Requirement requirement, ValidationRulesParser parser) {
		return new AdminRequirementResponse(
				requirement.getId(),
				requirement.getCode(),
				requirement.getName(),
				requirement.getDescription(),
				requirement.isMandatory(),
				parser.parse(requirement.getValidationRules()));
	}
}
