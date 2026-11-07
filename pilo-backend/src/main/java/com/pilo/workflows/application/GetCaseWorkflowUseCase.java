package com.pilo.workflows.application;

import com.pilo.users.Role;
import com.pilo.workflows.WorkflowResponse;
import com.pilo.workflows.WorkflowService;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetCaseWorkflowUseCase {

	private final WorkflowService workflowService;

	public GetCaseWorkflowUseCase(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	public WorkflowResponse execute(UUID caseId, UUID userId, Role role) {
		return workflowService.getWorkflow(caseId, userId, role);
	}
}
