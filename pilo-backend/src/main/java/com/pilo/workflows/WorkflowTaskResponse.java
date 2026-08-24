package com.pilo.workflows;

import java.util.UUID;

public record WorkflowTaskResponse(
		UUID id,
		String taskCode,
		String taskName,
		WorkflowTaskStatus status,
		String assignedRole,
		UUID dependsOnTaskId) {

	public static WorkflowTaskResponse from(WorkflowTask task) {
		return new WorkflowTaskResponse(
				task.getId(),
				task.getTaskCode(),
				task.getTaskName(),
				task.getStatus(),
				task.getAssignedRole().name(),
				task.getDependsOnTask() != null ? task.getDependsOnTask().getId() : null);
	}
}
