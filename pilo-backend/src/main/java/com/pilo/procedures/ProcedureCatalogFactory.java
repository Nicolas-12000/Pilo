package com.pilo.procedures;

import java.util.UUID;

final class ProcedureCatalogFactory {

	private ProcedureCatalogFactory() {}

	static UUID seedRentalRegistration(
			ProcedureTypeRepository procedureTypeRepository, RequirementRepository requirementRepository) {
		ProcedureType procedureType = new ProcedureType();
		procedureType.setTitle("Registro de contrato de alquiler");
		procedureType.setDescription(
				"Trámite para registrar un contrato de arrendamiento y validar la documentación requerida.");
		procedureType.setTargetDays(15);

		addRequirement(procedureType, "RENTAL_CONTRACT", "Contrato de alquiler", true);
		addRequirement(procedureType, "IDENTITY_DOCUMENT", "Documento de identidad", true);
		addRequirement(procedureType, "HOME_INSURANCE", "Seguro del hogar", false);

		procedureTypeRepository.save(procedureType);
		requirementRepository.flush();
		return procedureType.getId();
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
