package com.pilo.cases;

import com.pilo.cases.application.CreateCaseUseCase;
import com.pilo.cases.application.GetCaseDetailUseCase;
import com.pilo.cases.application.ListMyCasesUseCase;
import com.pilo.cases.application.ListPendingReviewCasesUseCase;
import com.pilo.common.AuthSupport;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
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

	private final CreateCaseUseCase createCaseUseCase;
	private final ListMyCasesUseCase listMyCasesUseCase;
	private final ListPendingReviewCasesUseCase listPendingReviewCasesUseCase;
	private final GetCaseDetailUseCase getCaseDetailUseCase;

	public CaseController(
			CreateCaseUseCase createCaseUseCase,
			ListMyCasesUseCase listMyCasesUseCase,
			ListPendingReviewCasesUseCase listPendingReviewCasesUseCase,
			GetCaseDetailUseCase getCaseDetailUseCase) {
		this.createCaseUseCase = createCaseUseCase;
		this.listMyCasesUseCase = listMyCasesUseCase;
		this.listPendingReviewCasesUseCase = listPendingReviewCasesUseCase;
		this.getCaseDetailUseCase = getCaseDetailUseCase;
	}

	@PostMapping
	public CaseDetailResponse create(@Valid @RequestBody CreateCaseRequest request, Authentication authentication) {
		return createCaseUseCase.execute(AuthSupport.userId(authentication), request);
	}

	@GetMapping
	public List<CaseSummaryResponse> listMine(Authentication authentication) {
		return listMyCasesUseCase.execute(AuthSupport.userId(authentication));
	}

	@GetMapping("/pending-review")
	@PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
	public List<PendingReviewCaseResponse> listPendingReview(Authentication authentication) {
		return listPendingReviewCasesUseCase.execute(AuthSupport.role(authentication));
	}

	@GetMapping("/{id}")
	public CaseDetailResponse get(@PathVariable UUID id, Authentication authentication) {
		return getCaseDetailUseCase.execute(id, AuthSupport.userId(authentication), AuthSupport.role(authentication));
	}
}
