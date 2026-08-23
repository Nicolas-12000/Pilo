package com.pilo.procedures;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "pilo.seed", name = "enabled", havingValue = "true")
public class LocalProcedureCatalogSeeder implements ApplicationRunner {

	private final ProcedureTypeRepository procedureTypeRepository;
	private final RequirementRepository requirementRepository;

	public LocalProcedureCatalogSeeder(
			ProcedureTypeRepository procedureTypeRepository, RequirementRepository requirementRepository) {
		this.procedureTypeRepository = procedureTypeRepository;
		this.requirementRepository = requirementRepository;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (procedureTypeRepository.count() > 0) {
			return;
		}

		ProcedureCatalogFactory.seedRentalRegistration(procedureTypeRepository, requirementRepository);
		seedEmpadronamiento();
	}

	private void seedEmpadronamiento() {
		ProcedureType procedureType = new ProcedureType();
		procedureType.setTitle("Solicitud de empadronamiento");
		procedureType.setDescription(
				"Trámite para solicitar el alta en el padrón municipal con la documentación acreditativa.");
		procedureType.setTargetDays(10);

		addRequirement(procedureType, "IDENTITY_DOCUMENT", "Documento de identidad", true);
		addRequirement(procedureType, "PROOF_OF_ADDRESS", "Justificante de domicilio", true);

		procedureTypeRepository.save(procedureType);
	}

	private static void addRequirement(ProcedureType procedureType, String code, String name, boolean mandatory) {
		Requirement requirement = new Requirement();
		requirement.setProcedureType(procedureType);
		requirement.setCode(code);
		requirement.setName(name);
		requirement.setDescription("Requisito " + name.toLowerCase() + " para el trámite.");
		requirement.setMandatory(mandatory);
		requirement.setValidationRules("{}");
		procedureType.getRequirements().add(requirement);
	}
}
