package com.pilo.cases.application;

import com.pilo.cases.CaseService;
import com.pilo.cases.PendingReviewCaseResponse;
import com.pilo.users.Role;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListPendingReviewCasesUseCase {

	private final CaseService caseService;

	public ListPendingReviewCasesUseCase(CaseService caseService) {
		this.caseService = caseService;
	}

	public List<PendingReviewCaseResponse> execute(Role role) {
		return caseService.listPendingReview(role);
	}
}
