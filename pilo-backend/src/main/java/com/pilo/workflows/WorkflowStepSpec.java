package com.pilo.workflows;

import com.pilo.users.Role;

public record WorkflowStepSpec(
		String taskCode,
		String displayName,
		Role assignedRole,
		String dependsOnTaskCode,
		CompletionRule completionRule,
		int sortOrder) {}
