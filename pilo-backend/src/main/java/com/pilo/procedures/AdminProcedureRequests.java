package com.pilo.procedures;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

record CreateProcedureTypeRequest(
		@NotBlank @Size(max = 160) String title,
		@NotBlank @Size(max = 4000) String description,
		@Positive int targetDays) {}

record UpdateProcedureTypeRequest(
		@NotBlank @Size(max = 160) String title,
		@NotBlank @Size(max = 4000) String description,
		@Positive int targetDays) {}

record CreateRequirementRequest(
		@NotBlank @Size(max = 64) String code,
		@NotBlank @Size(max = 160) String name,
		@NotBlank @Size(max = 4000) String description,
		boolean mandatory,
		@NotNull @Valid AdminValidationRulesRequest validationRules) {}

record UpdateRequirementRequest(
		@NotBlank @Size(max = 160) String name,
		@NotBlank @Size(max = 4000) String description,
		boolean mandatory,
		@NotNull @Valid AdminValidationRulesRequest validationRules) {}

record AdminValidationRulesRequest(
		@NotBlank @Size(max = 128) String expectedDocumentType,
		boolean requireFutureExpiration,
		Double minConfidence) {}
