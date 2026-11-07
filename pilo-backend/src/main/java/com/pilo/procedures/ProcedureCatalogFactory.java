package com.pilo.procedures;

import com.pilo.workflows.WorkflowBlueprints;
import com.pilo.workflows.WorkflowDefinitionService;
import java.util.UUID;

final class ProcedureCatalogFactory {

	static final String RENTAL_REGISTRATION = "Registro de contrato de alquiler";
	static final String EMPADRONAMIENTO = "Solicitud de empadronamiento";
	static final String OBRA_MENOR = "Licencia de obra menor";
	static final String BONO_ALQUILER_JOVEN = "Bono Alquiler Joven";

	private static final String IDENTITY_RULES = """
			{"expectedDocumentType":"identity_document","minConfidence":0.7}
			""";

	private ProcedureCatalogFactory() {}

	static UUID seedRentalRegistration(
			ProcedureTypeRepository procedureTypeRepository,
			WorkflowDefinitionService workflowDefinitionService) {
		ProcedureType procedureType = procedure(
				RENTAL_REGISTRATION,
				"Registro del contrato de arrendamiento de vivienda y depósito de la documentación que lo acredita.",
				15);
		addRequirement(
				procedureType,
				"RENTAL_CONTRACT",
				"Contrato de alquiler",
				"Contrato firmado por arrendador y arrendatario, con la duración y la renta pactadas.",
				true,
				"""
				{"expectedDocumentType":"rental_contract","requireFutureExpiration":true}
				""");
		addRequirement(
				procedureType,
				"IDENTITY_DOCUMENT",
				"Documento de identidad",
				"DNI o NIE en vigor de la persona arrendataria.",
				true,
				IDENTITY_RULES);
		addRequirement(
				procedureType,
				"HOME_INSURANCE",
				"Seguro del hogar",
				"Póliza del seguro de la vivienda, si el contrato lo exige.",
				false,
				"""
				{"expectedDocumentType":"home_insurance"}
				""");
		return save(procedureType, procedureTypeRepository, workflowDefinitionService);
	}

	static UUID seedEmpadronamiento(
			ProcedureTypeRepository procedureTypeRepository,
			WorkflowDefinitionService workflowDefinitionService) {
		ProcedureType procedureType = procedure(
				EMPADRONAMIENTO,
				"Alta en el padrón municipal del domicilio donde se reside de forma habitual.",
				10);
		addRequirement(
				procedureType,
				"IDENTITY_DOCUMENT",
				"Documento de identidad",
				"DNI, NIE o pasaporte en vigor de la persona que se empadrona.",
				true,
				IDENTITY_RULES);
		addRequirement(
				procedureType,
				"PROOF_OF_ADDRESS",
				"Justificante de domicilio",
				"Escritura, contrato de alquiler o recibo de suministro a nombre del solicitante.",
				true,
				"""
				{"expectedDocumentType":"proof_of_address"}
				""");
		return save(procedureType, procedureTypeRepository, workflowDefinitionService);
	}

	static UUID seedObraMenor(
			ProcedureTypeRepository procedureTypeRepository,
			WorkflowDefinitionService workflowDefinitionService) {
		ProcedureType procedureType = procedure(
				OBRA_MENOR,
				"Autorización municipal para reformas sin afección estructural: cocina, baño, suelos o fachada.",
				30);
		addRequirement(
				procedureType,
				"IDENTITY_DOCUMENT",
				"Documento de identidad",
				"DNI o NIE en vigor de la persona promotora de la obra.",
				true,
				IDENTITY_RULES);
		addRequirement(
				procedureType,
				"WORK_DESCRIPTION",
				"Memoria y presupuesto de la obra",
				"Descripción de los trabajos, su ubicación y el presupuesto de ejecución.",
				true,
				"""
				{"expectedDocumentType":"building_plan"}
				""");
		addRequirement(
				procedureType,
				"FEE_RECEIPT",
				"Justificante de pago de la tasa",
				"Autoliquidación de la tasa por licencia urbanística pagada.",
				true,
				"""
				{"expectedDocumentType":"tax_certificate"}
				""");
		return save(procedureType, procedureTypeRepository, workflowDefinitionService);
	}

	static UUID seedBonoAlquilerJoven(
			ProcedureTypeRepository procedureTypeRepository,
			WorkflowDefinitionService workflowDefinitionService) {
		ProcedureType procedureType = procedure(
				BONO_ALQUILER_JOVEN,
				"Ayuda mensual al alquiler de vivienda habitual para personas de 18 a 35 años con ingresos limitados.",
				45);
		addRequirement(
				procedureType,
				"IDENTITY_DOCUMENT",
				"Documento de identidad",
				"DNI o NIE en vigor de la persona solicitante.",
				true,
				IDENTITY_RULES);
		addRequirement(
				procedureType,
				"RENTAL_CONTRACT",
				"Contrato de alquiler",
				"Contrato de arrendamiento de la vivienda habitual a nombre del solicitante.",
				true,
				"""
				{"expectedDocumentType":"rental_contract","requireFutureExpiration":true}
				""");
		addRequirement(
				procedureType,
				"PROOF_OF_ADDRESS",
				"Certificado de empadronamiento",
				"Certificado que acredite la residencia en la vivienda alquilada.",
				true,
				"""
				{"expectedDocumentType":"proof_of_address"}
				""");
		addRequirement(
				procedureType,
				"INCOME_CERTIFICATE",
				"Certificado de ingresos",
				"Certificado de IRPF o de imputaciones de la Agencia Tributaria del último ejercicio.",
				true,
				"""
				{"expectedDocumentType":"tax_certificate"}
				""");
		return save(procedureType, procedureTypeRepository, workflowDefinitionService);
	}

	private static ProcedureType procedure(String title, String description, int targetDays) {
		ProcedureType procedureType = new ProcedureType();
		procedureType.setTitle(title);
		procedureType.setDescription(description);
		procedureType.setTargetDays(targetDays);
		return procedureType;
	}

	private static UUID save(
			ProcedureType procedureType,
			ProcedureTypeRepository procedureTypeRepository,
			WorkflowDefinitionService workflowDefinitionService) {
		procedureTypeRepository.save(procedureType);
		workflowDefinitionService.attach(procedureType, WorkflowBlueprints.documentCollectionReview());
		return procedureType.getId();
	}

	private static void addRequirement(
			ProcedureType procedureType,
			String code,
			String name,
			String description,
			boolean mandatory,
			String validationRules) {
		Requirement requirement = new Requirement();
		requirement.setProcedureType(procedureType);
		requirement.setCode(code);
		requirement.setName(name);
		requirement.setDescription(description);
		requirement.setMandatory(mandatory);
		requirement.setValidationRules(validationRules);
		procedureType.getRequirements().add(requirement);
	}
}
