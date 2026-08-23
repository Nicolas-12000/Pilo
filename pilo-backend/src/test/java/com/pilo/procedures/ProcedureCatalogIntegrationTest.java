package com.pilo.procedures;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ProcedureCatalogIntegrationTest extends com.pilo.support.AbstractIntegrationTest {

	@Autowired
	protected ProcedureTypeRepository procedureTypeRepository;

	@Autowired
	protected RequirementRepository requirementRepository;

	protected UUID procedureTypeId;

	@BeforeEach
	void seedProcedureCatalog() {
		procedureTypeId = ProcedureCatalogFactory.seedRentalRegistration(procedureTypeRepository, requirementRepository);
	}
}
