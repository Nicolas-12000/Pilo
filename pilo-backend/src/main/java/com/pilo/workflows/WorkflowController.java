package com.pilo.workflows;

import com.pilo.cases.CaseDetailResponse;
import com.pilo.cases.CaseService;
import com.pilo.common.AuthSupport;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cases/{caseId}/workflow")
public class WorkflowController {

	private final WorkflowService workflowService;
	private final CaseService caseService;

	public WorkflowController(WorkflowService workflowService, CaseService caseService) {
		this.workflowService = workflowService;
		this.caseService = caseService;
	}

	@GetMapping
	public WorkflowResponse get(@PathVariable UUID caseId, Authentication authentication) {
		return workflowService.getWorkflow(caseId, AuthSupport.userId(authentication), AuthSupport.role(authentication));
	}

	@PostMapping("/final-review")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
	public CaseDetailResponse completeFinalReview(
			@PathVariable UUID caseId,
			@Valid @RequestBody FinalReviewRequest request,
			Authentication authentication) {
		workflowService.completeFinalReview(
				caseId, AuthSupport.userId(authentication), AuthSupport.role(authentication), request);
		return caseService.getCaseDetail(caseId, AuthSupport.userId(authentication), AuthSupport.role(authentication));
	}
}
