package com.pilo.workflows;

import com.pilo.cases.CaseDetailResponse;
import com.pilo.common.AuthSupport;
import com.pilo.workflows.application.CompleteFinalReviewUseCase;
import com.pilo.workflows.application.GetCaseWorkflowUseCase;
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

	private final GetCaseWorkflowUseCase getCaseWorkflowUseCase;
	private final CompleteFinalReviewUseCase completeFinalReviewUseCase;

	public WorkflowController(
			GetCaseWorkflowUseCase getCaseWorkflowUseCase, CompleteFinalReviewUseCase completeFinalReviewUseCase) {
		this.getCaseWorkflowUseCase = getCaseWorkflowUseCase;
		this.completeFinalReviewUseCase = completeFinalReviewUseCase;
	}

	@GetMapping
	public WorkflowResponse get(@PathVariable UUID caseId, Authentication authentication) {
		return getCaseWorkflowUseCase.execute(caseId, AuthSupport.userId(authentication), AuthSupport.role(authentication));
	}

	@PostMapping("/final-review")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
	public CaseDetailResponse completeFinalReview(
			@PathVariable UUID caseId,
			@Valid @RequestBody FinalReviewRequest request,
			Authentication authentication) {
		return completeFinalReviewUseCase.execute(
				caseId, AuthSupport.userId(authentication), AuthSupport.role(authentication), request);
	}
}
