package com.pilo.procedures;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/procedures/types")
public class ProcedureTypeController {

	private final ProcedureTypeService procedureTypeService;

	public ProcedureTypeController(ProcedureTypeService procedureTypeService) {
		this.procedureTypeService = procedureTypeService;
	}

	@GetMapping
	public List<ProcedureTypeSummaryResponse> list() {
		return procedureTypeService.listAll();
	}

	@GetMapping("/{id}")
	public ProcedureTypeDetailResponse get(@PathVariable UUID id) {
		return procedureTypeService.getById(id);
	}
}
