package com.pilo.cases.application;

import com.pilo.cases.CaseService;
import com.pilo.cases.CaseSummaryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ListMyCasesUseCase {

	private final CaseService caseService;

	public ListMyCasesUseCase(CaseService caseService) {
		this.caseService = caseService;
	}

	public List<CaseSummaryResponse> execute(UUID userId) {
		return caseService.listMyCases(userId);
	}
}
