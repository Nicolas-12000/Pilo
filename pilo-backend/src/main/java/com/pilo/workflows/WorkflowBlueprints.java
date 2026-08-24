package com.pilo.workflows;

import com.pilo.users.Role;
import java.util.List;

public final class WorkflowBlueprints {

	private WorkflowBlueprints() {}

	public static List<WorkflowStepSpec> documentCollectionReview() {
		return List.of(
				new WorkflowStepSpec("OPEN_CASE", "Apertura del expediente", Role.USER, null, CompletionRule.ON_CREATE, 10),
				new WorkflowStepSpec(
						"COLLECT_DOCUMENTS",
						"Recopilar documentación",
						Role.USER,
						"OPEN_CASE",
						CompletionRule.ALL_MANDATORY_PRESENT,
						20),
				new WorkflowStepSpec(
						"VALIDATE_DOCUMENTS",
						"Validar documentación",
						Role.REVIEWER,
						"COLLECT_DOCUMENTS",
						CompletionRule.ALL_MANDATORY_VALIDATED,
						30),
				new WorkflowStepSpec(
						"FINAL_REVIEW",
						"Revisión final",
						Role.REVIEWER,
						"VALIDATE_DOCUMENTS",
						CompletionRule.MANUAL,
						40));
	}
}
