package com.pilo.procedures;

import java.util.UUID;

public record RequirementResponse(
		UUID id, String code, String name, String description, boolean mandatory) {

	public static RequirementResponse from(Requirement requirement) {
		return new RequirementResponse(
				requirement.getId(),
				requirement.getCode(),
				requirement.getName(),
				requirement.getDescription(),
				requirement.isMandatory());
	}
}
