package com.pilo.procedures;

import com.pilo.common.AuthSupport;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/procedures/types")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProcedureTypeController {

	private final AdminProcedureCatalogService adminProcedureCatalogService;

	public AdminProcedureTypeController(AdminProcedureCatalogService adminProcedureCatalogService) {
		this.adminProcedureCatalogService = adminProcedureCatalogService;
	}

	@GetMapping
	public List<ProcedureTypeSummaryResponse> list() {
		return adminProcedureCatalogService.listAll();
	}

	@GetMapping("/{id}")
	public AdminProcedureTypeDetailResponse get(@PathVariable UUID id) {
		return adminProcedureCatalogService.getById(id);
	}

	@PostMapping
	public AdminProcedureTypeDetailResponse create(
			@Valid @RequestBody CreateProcedureTypeRequest request, Authentication authentication) {
		return adminProcedureCatalogService.create(AuthSupport.userId(authentication), request);
	}

	@PutMapping("/{id}")
	public AdminProcedureTypeDetailResponse update(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateProcedureTypeRequest request,
			Authentication authentication) {
		return adminProcedureCatalogService.update(AuthSupport.userId(authentication), id, request);
	}

	@PostMapping("/{id}/requirements")
	public AdminRequirementResponse addRequirement(
			@PathVariable UUID id,
			@Valid @RequestBody CreateRequirementRequest request,
			Authentication authentication) {
		return adminProcedureCatalogService.addRequirement(AuthSupport.userId(authentication), id, request);
	}

	@PutMapping("/{id}/requirements/{requirementId}")
	public AdminRequirementResponse updateRequirement(
			@PathVariable UUID id,
			@PathVariable UUID requirementId,
			@Valid @RequestBody UpdateRequirementRequest request,
			Authentication authentication) {
		return adminProcedureCatalogService.updateRequirement(
				AuthSupport.userId(authentication), id, requirementId, request);
	}

	@DeleteMapping("/{id}/requirements/{requirementId}")
	public void deleteRequirement(
			@PathVariable UUID id, @PathVariable UUID requirementId, Authentication authentication) {
		adminProcedureCatalogService.deleteRequirement(AuthSupport.userId(authentication), id, requirementId);
	}
}
