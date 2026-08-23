package com.pilo.procedures;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcedureTypeService {

	private final ProcedureTypeRepository procedureTypeRepository;

	public ProcedureTypeService(ProcedureTypeRepository procedureTypeRepository) {
		this.procedureTypeRepository = procedureTypeRepository;
	}

	@Transactional(readOnly = true)
	public List<ProcedureTypeSummaryResponse> listAll() {
		return procedureTypeRepository.findAllWithRequirements().stream()
				.map(ProcedureTypeSummaryResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public ProcedureTypeDetailResponse getById(UUID id) {
		ProcedureType procedureType = procedureTypeRepository
				.findByIdWithRequirements(id)
				.orElseThrow(ProcedureTypeNotFoundException::new);
		return ProcedureTypeDetailResponse.from(procedureType);
	}
}
