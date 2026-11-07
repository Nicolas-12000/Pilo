package com.pilo.cases.application;

import com.pilo.cases.CaseDetailResponse;
import com.pilo.cases.CaseService;
import com.pilo.users.Role;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetCaseDetailUseCase {

	private final CaseService caseService;

	public GetCaseDetailUseCase(CaseService caseService) {
		this.caseService = caseService;
	}

	public CaseDetailResponse execute(UUID caseId, UUID userId, Role role) {
		return caseService.getCaseDetail(caseId, userId, role);
	}
}
