package com.pilo.workflows;

import com.pilo.common.AuthSupport;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cases/{caseId}/workflow")
public class WorkflowController {

	private final WorkflowService workflowService;

	public WorkflowController(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	@GetMapping
	public WorkflowResponse get(@PathVariable UUID caseId, Authentication authentication) {
		return workflowService.getWorkflow(caseId, AuthSupport.userId(authentication), AuthSupport.role(authentication));
	}
}
