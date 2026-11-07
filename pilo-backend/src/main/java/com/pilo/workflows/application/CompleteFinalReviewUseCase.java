package com.pilo.workflows.application;

import com.pilo.cases.CaseDetailResponse;
import com.pilo.cases.application.GetCaseDetailUseCase;
import com.pilo.users.Role;
import com.pilo.workflows.FinalReviewRequest;
import com.pilo.workflows.WorkflowService;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CompleteFinalReviewUseCase {

	private final WorkflowService workflowService;
	private final GetCaseDetailUseCase getCaseDetailUseCase;

	public CompleteFinalReviewUseCase(WorkflowService workflowService, GetCaseDetailUseCase getCaseDetailUseCase) {
		this.workflowService = workflowService;
		this.getCaseDetailUseCase = getCaseDetailUseCase;
	}

	public CaseDetailResponse execute(UUID caseId, UUID reviewerId, Role role, FinalReviewRequest request) {
		workflowService.completeFinalReview(caseId, reviewerId, role, request);
		return getCaseDetailUseCase.execute(caseId, reviewerId, role);
	}
}
