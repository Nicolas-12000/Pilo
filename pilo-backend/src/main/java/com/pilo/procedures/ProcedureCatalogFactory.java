package com.pilo.procedures;

import com.pilo.workflows.WorkflowBlueprints;
import com.pilo.workflows.WorkflowDefinitionService;
import java.util.UUID;

final class ProcedureCatalogFactory {

	private ProcedureCatalogFactory() {}

	static UUID seedRentalRegistration(
			ProcedureTypeRepository procedureTypeRepository,
			WorkflowDefinitionService workflowDefinitionService) {
		ProcedureType procedureType = new ProcedureType();
		procedureType.setTitle("Registro de contrato de alquiler");
		procedureType.setDescription(
				"Trámite para registrar un contrato de arrendamiento y validar la documentación requerida.");
		procedureType.setTargetDays(15);

		addRequirement(
				procedureType,
				"RENTAL_CONTRACT",
				"Contrato de alquiler",
				true,
				"""
				{"expectedDocumentType":"rental_contract","requireFutureExpiration":true}
				""");
		addRequirement(
				procedureType,
				"IDENTITY_DOCUMENT",
				"Documento de identidad",
				true,
				"""
				{"expectedDocumentType":"identity_document"}
				""");
		addRequirement(
				procedureType,
				"HOME_INSURANCE",
				"Seguro del hogar",
				false,
				"""
				{"expectedDocumentType":"home_insurance"}
				""");

		procedureTypeRepository.save(procedureType);
		workflowDefinitionService.attach(procedureType, WorkflowBlueprints.documentCollectionReview());
		return procedureType.getId();
	}

	static UUID seedEmpadronamiento(
			ProcedureTypeRepository procedureTypeRepository,
			WorkflowDefinitionService workflowDefinitionService) {
		ProcedureType procedureType = new ProcedureType();
		procedureType.setTitle("Solicitud de empadronamiento");
		procedureType.setDescription(
				"Trámite para solicitar el alta en el padrón municipal con la documentación acreditativa.");
		procedureType.setTargetDays(10);

		addRequirement(
				procedureType,
				"IDENTITY_DOCUMENT",
				"Documento de identidad",
				true,
				"""
				{"expectedDocumentType":"identity_document"}
				""");
		addRequirement(
				procedureType,
				"PROOF_OF_ADDRESS",
				"Justificante de domicilio",
				true,
				"""
				{"expectedDocumentType":"proof_of_address"}
				""");

		procedureTypeRepository.save(procedureType);
		workflowDefinitionService.attach(procedureType, WorkflowBlueprints.documentCollectionReview());
		return procedureType.getId();
	}

	private static void addRequirement(
			ProcedureType procedureType, String code, String name, boolean mandatory, String validationRules) {
		Requirement requirement = new Requirement();
		requirement.setProcedureType(procedureType);
		requirement.setCode(code);
		requirement.setName(name);
		requirement.setDescription("Requisito " + name.toLowerCase() + " para el trámite.");
		requirement.setMandatory(mandatory);
		requirement.setValidationRules(validationRules);
		procedureType.getRequirements().add(requirement);
	}
}
