package com.pilo.cases.application;

import com.pilo.cases.CaseDetailResponse;
import com.pilo.cases.CaseService;
import com.pilo.cases.CreateCaseRequest;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CreateCaseUseCase {

	private final CaseService caseService;

	public CreateCaseUseCase(CaseService caseService) {
		this.caseService = caseService;
	}

	public CaseDetailResponse execute(UUID userId, CreateCaseRequest request) {
		return caseService.createCase(userId, request);
	}
}
