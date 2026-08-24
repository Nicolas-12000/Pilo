package com.pilo.workflows;

import com.pilo.cases.ProcedureCase;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class WorkflowInitializer {

	private final WorkflowDefinitionService workflowDefinitionService;
	private final WorkflowTaskRepository workflowTaskRepository;

	public WorkflowInitializer(
			WorkflowDefinitionService workflowDefinitionService, WorkflowTaskRepository workflowTaskRepository) {
		this.workflowDefinitionService = workflowDefinitionService;
		this.workflowTaskRepository = workflowTaskRepository;
	}

	public void initializeFor(ProcedureCase procedureCase) {
		List<WorkflowStepDefinition> definitions =
				workflowDefinitionService.requireDefinitions(procedureCase.getProcedureType());
		Map<String, WorkflowTask> tasksByCode = new HashMap<>();

		for (WorkflowStepDefinition definition : definitions) {
			WorkflowTask task = new WorkflowTask();
			task.setProcedureCase(procedureCase);
			task.setTaskCode(definition.getTaskCode());
			task.setTaskName(definition.getDisplayName());
			task.setSortOrder(definition.getSortOrder());
			task.setCompletionRule(definition.getCompletionRule());
			task.setAssignedRole(definition.getAssignedRole());
			task.setStatus(initialStatus(definition));
			task.setUpdatedAt(Instant.now());
			if (definition.getDependsOnTaskCode() != null) {
				task.setDependsOnTask(tasksByCode.get(definition.getDependsOnTaskCode()));
			}
			workflowTaskRepository.save(task);
			tasksByCode.put(definition.getTaskCode(), task);
		}
	}

	private static WorkflowTaskStatus initialStatus(WorkflowStepDefinition definition) {
		if (definition.getCompletionRule() == CompletionRule.ON_CREATE) {
			return WorkflowTaskStatus.COMPLETED;
		}
		if (definition.getDependsOnTaskCode() == null) {
			return WorkflowTaskStatus.IN_PROGRESS;
		}
		return WorkflowTaskStatus.PENDING;
	}
}
