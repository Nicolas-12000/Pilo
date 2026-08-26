package com.pilo.procedures;

import com.pilo.common.CacheNames;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcedureTypeService {

	private final ProcedureTypeRepository procedureTypeRepository;

	public ProcedureTypeService(ProcedureTypeRepository procedureTypeRepository) {
		this.procedureTypeRepository = procedureTypeRepository;
	}

	// Public, read-only catalog: safe to cache in-process. Evicted by AdminProcedureCatalogService
	// whenever a procedure type or requirement changes, so no stale-data risk under normal writes.
	@Cacheable(cacheNames = CacheNames.PROCEDURE_CATALOG, key = "'list'")
	@Transactional(readOnly = true)
	public List<ProcedureTypeSummaryResponse> listAll() {
		return procedureTypeRepository.findAllWithRequirements().stream()
				.map(ProcedureTypeSummaryResponse::from)
				.toList();
	}

	@Cacheable(cacheNames = CacheNames.PROCEDURE_CATALOG, key = "#id")
	@Transactional(readOnly = true)
	public ProcedureTypeDetailResponse getById(UUID id) {
		ProcedureType procedureType = procedureTypeRepository
				.findByIdWithRequirements(id)
				.orElseThrow(ProcedureTypeNotFoundException::new);
		return ProcedureTypeDetailResponse.from(procedureType);
	}
}
