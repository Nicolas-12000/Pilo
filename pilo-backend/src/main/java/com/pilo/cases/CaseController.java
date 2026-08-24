package com.pilo.cases;

import com.pilo.common.AuthSupport;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cases")
public class CaseController {

	private final CaseService caseService;

	public CaseController(CaseService caseService) {
		this.caseService = caseService;
	}

	@PostMapping
	public CaseDetailResponse create(@Valid @RequestBody CreateCaseRequest request, Authentication authentication) {
		return caseService.createCase(AuthSupport.userId(authentication), request);
	}

	@GetMapping
	public List<CaseSummaryResponse> listMine(Authentication authentication) {
		return caseService.listMyCases(AuthSupport.userId(authentication));
	}

	@GetMapping("/{id}")
	public CaseDetailResponse get(@PathVariable UUID id, Authentication authentication) {
		return caseService.getCaseDetail(id, AuthSupport.userId(authentication), AuthSupport.role(authentication));
	}
}
