package com.pilo.procedures;

import com.pilo.workflows.WorkflowBlueprints;
import com.pilo.workflows.WorkflowDefinitionService;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
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
		procedureTypeRepository.findAll().forEach(procedureType ->
				workflowDefinitionService.attach(procedureType, WorkflowBlueprints.documentCollectionReview()));

		// Seed by title so catalogues created before a sample was added still receive it.
		Map<String, BiFunction<ProcedureTypeRepository, WorkflowDefinitionService, ?>> samples = new LinkedHashMap<>();
		samples.put(ProcedureCatalogFactory.RENTAL_REGISTRATION, ProcedureCatalogFactory::seedRentalRegistration);
		samples.put(ProcedureCatalogFactory.EMPADRONAMIENTO, ProcedureCatalogFactory::seedEmpadronamiento);
		samples.put(ProcedureCatalogFactory.OBRA_MENOR, ProcedureCatalogFactory::seedObraMenor);
		samples.put(ProcedureCatalogFactory.BONO_ALQUILER_JOVEN, ProcedureCatalogFactory::seedBonoAlquilerJoven);
		samples.forEach((title, seed) -> {
			if (!procedureTypeRepository.existsByTitleIgnoreCase(title)) {
				seed.apply(procedureTypeRepository, workflowDefinitionService);
			}
		});
	}
}
