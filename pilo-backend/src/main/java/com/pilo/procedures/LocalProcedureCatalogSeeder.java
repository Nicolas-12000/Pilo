package com.pilo.procedures;

import com.pilo.workflows.WorkflowBlueprints;
import com.pilo.workflows.WorkflowDefinitionService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "pilo.seed", name = "enabled", havingValue = "true")
public class LocalProcedureCatalogSeeder implements ApplicationRunner {

	private final ProcedureTypeRepository procedureTypeRepository;
	private final WorkflowDefinitionService workflowDefinitionService;

	public LocalProcedureCatalogSeeder(
			ProcedureTypeRepository procedureTypeRepository, WorkflowDefinitionService workflowDefinitionService) {
		this.procedureTypeRepository = procedureTypeRepository;
		this.workflowDefinitionService = workflowDefinitionService;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (procedureTypeRepository.count() == 0) {
			ProcedureCatalogFactory.seedRentalRegistration(procedureTypeRepository, workflowDefinitionService);
			ProcedureCatalogFactory.seedEmpadronamiento(procedureTypeRepository, workflowDefinitionService);
			return;
		}

		procedureTypeRepository.findAll().forEach(procedureType ->
				workflowDefinitionService.attach(procedureType, WorkflowBlueprints.documentCollectionReview()));
	}
}
