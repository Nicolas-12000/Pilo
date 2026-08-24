package com.pilo.workflows;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowStepDefinitionRepository extends JpaRepository<WorkflowStepDefinition, UUID> {

	List<WorkflowStepDefinition> findByProcedureTypeIdOrderBySortOrderAsc(UUID procedureTypeId);

	boolean existsByProcedureTypeId(UUID procedureTypeId);
}
