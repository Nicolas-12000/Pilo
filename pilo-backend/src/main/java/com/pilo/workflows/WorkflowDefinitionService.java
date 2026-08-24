package com.pilo.workflows;

import com.pilo.procedures.ProcedureType;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WorkflowDefinitionService {

	private final WorkflowStepDefinitionRepository workflowStepDefinitionRepository;

	public WorkflowDefinitionService(WorkflowStepDefinitionRepository workflowStepDefinitionRepository) {
		this.workflowStepDefinitionRepository = workflowStepDefinitionRepository;
	}

	public void attach(ProcedureType procedureType, List<WorkflowStepSpec> steps) {
		if (workflowStepDefinitionRepository.existsByProcedureTypeId(procedureType.getId())) {
			return;
		}
		List<WorkflowStepDefinition> definitions = steps.stream()
				.map(spec -> toDefinition(procedureType, spec))
				.toList();
		workflowStepDefinitionRepository.saveAll(definitions);
	}

	public List<WorkflowStepDefinition> requireDefinitions(ProcedureType procedureType) {
		List<WorkflowStepDefinition> definitions =
				workflowStepDefinitionRepository.findByProcedureTypeIdOrderBySortOrderAsc(procedureType.getId());
		if (definitions.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "WORKFLOW_DEFINITION_MISSING");
		}
		return definitions;
	}

	private static WorkflowStepDefinition toDefinition(ProcedureType procedureType, WorkflowStepSpec spec) {
		WorkflowStepDefinition definition = new WorkflowStepDefinition();
		definition.setProcedureType(procedureType);
		definition.setTaskCode(spec.taskCode());
		definition.setDisplayName(spec.displayName());
		definition.setAssignedRole(spec.assignedRole());
		definition.setSortOrder(spec.sortOrder());
		definition.setDependsOnTaskCode(spec.dependsOnTaskCode());
		definition.setCompletionRule(spec.completionRule());
		return definition;
	}
}
