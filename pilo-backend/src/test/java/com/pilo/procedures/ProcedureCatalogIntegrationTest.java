package com.pilo.procedures;

import com.pilo.workflows.WorkflowDefinitionService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ProcedureCatalogIntegrationTest extends com.pilo.support.AbstractIntegrationTest {

	@Autowired
	protected ProcedureTypeRepository procedureTypeRepository;

	@Autowired
	protected WorkflowDefinitionService workflowDefinitionService;

	protected UUID procedureTypeId;

	@BeforeEach
	void seedProcedureCatalog() {
		procedureTypeId =
				ProcedureCatalogFactory.seedRentalRegistration(procedureTypeRepository, workflowDefinitionService);
	}
}
