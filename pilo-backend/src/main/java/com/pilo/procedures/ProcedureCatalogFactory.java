package com.pilo.procedures;

import com.pilo.workflows.WorkflowBlueprints;
import com.pilo.workflows.WorkflowDefinitionService;
import java.util.UUID;

final class ProcedureCatalogFactory {

	static final String RENTAL_REGISTRATION = "Registro de contrato de alquiler";
	static final String EMPADRONAMIENTO = "Solicitud de empadronamiento";
	static final String OBRA_MENOR = "Licencia de obra menor";
	static final String BONO_ALQUILER_JOVEN = "Bono Alquiler Joven";
	static final String CERTIFICADO_EMPADRONAMIENTO = "Certificado de empadronamiento";
	static final String LICENCIA_APERTURA = "Licencia de apertura de actividad";
	static final String CEDULA_HABITABILIDAD = "Cédula de habitabilidad";
	static final String RENOVACION_DNI = "Renovación del DNI";
	static final String TARJETA_SANITARIA = "Solicitud de tarjeta sanitaria";
	static final String ALTA_AUTONOMO = "Alta como autónomo en la Seguridad Social";
	static final String CAMBIO_TITULAR_SUMINISTROS = "Cambio de titularidad del suministro eléctrico";
	static final String PAREJA_DE_HECHO = "Inscripción en el Registro de Parejas de Hecho";
	static final String GRADO_DEPENDENCIA = "Solicitud de reconocimiento del grado de dependencia";
	static final String BONIFICACION_IBI = "Solicitud de bonificación del IBI";

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

	static UUID seedCertificadoEmpadronamiento(
			ProcedureTypeRepository procedureTypeRepository,
			WorkflowDefinitionService workflowDefinitionService) {
		ProcedureType procedureType = procedure(
				CERTIFICADO_EMPADRONAMIENTO,
				"Expedición del certificado acreditativo de estar inscrito en el padrón municipal.",
				5);
		addRequirement(
				procedureType,
				"IDENTITY_DOCUMENT",
				"Documento de identidad",
				"DNI, NIE o pasaporte en vigor de la persona solicitante.",
				true,
				IDENTITY_RULES);
		return save(procedureType, procedureTypeRepository, workflowDefinitionService);
	}

	static UUID seedLicenciaApertura(
			ProcedureTypeRepository procedureTypeRepository,
			WorkflowDefinitionService workflowDefinitionService) {
		ProcedureType procedureType = procedure(
				LICENCIA_APERTURA,
				"Autorización municipal para iniciar una actividad económica en un local o establecimiento.",
				20);
		addRequirement(
				procedureType,
				"IDENTITY_DOCUMENT",
				"Documento de identidad",
				"DNI o NIE en vigor del titular o representante.",
				true,
				IDENTITY_RULES);
		addRequirement(
				procedureType,
				"PROOF_OF_ADDRESS",
				"Título del local",
				"Escritura, contrato de arrendamiento o autorización de uso del local.",
				true,
				"""
				{"expectedDocumentType":"proof_of_address"}
				""");
		addRequirement(
				procedureType,
				"TAX_CERTIFICATE",
				"Alta censal o IAE",
				"Documento acreditativo del alta en el censo de actividades o en el IAE.",
				true,
				"""
				{"expectedDocumentType":"tax_certificate"}
				""");
		return save(procedureType, procedureTypeRepository, workflowDefinitionService);
	}

	static UUID seedCedulaHabitabilidad(
			ProcedureTypeRepository procedureTypeRepository,
			WorkflowDefinitionService workflowDefinitionService) {
		ProcedureType procedureType = procedure(
				CEDULA_HABITABILIDAD,
				"Certificación de que una vivienda cumple condiciones mínimas de habitabilidad para su uso.",
				25);
		addRequirement(
				procedureType,
				"IDENTITY_DOCUMENT",
				"Documento de identidad",
				"DNI o NIE en vigor del propietario o arrendatario.",
				true,
				IDENTITY_RULES);
		addRequirement(
				procedureType,
				"WORK_DESCRIPTION",
				"Proyecto o memoria técnica",
				"Memoria descriptiva de la vivienda y planos cuando el ayuntamiento los exija.",
				true,
				"""
				{"expectedDocumentType":"building_plan"}
				""");
		return save(procedureType, procedureTypeRepository, workflowDefinitionService);
	}

	static UUID seedRenovacionDni(
			ProcedureTypeRepository procedureTypeRepository,
			WorkflowDefinitionService workflowDefinitionService) {
		ProcedureType procedureType = procedure(
				RENOVACION_DNI,
				"Renovación del Documento Nacional de Identidad por caducidad o cambio de datos en comisaría o oficina.",
				15);
		addRequirement(
				procedureType,
				"IDENTITY_DOCUMENT",
				"DNI anterior o en vigor",
				"DNI caducado o próximo a caducar del solicitante.",
				true,
				IDENTITY_RULES);
		addRequirement(
				procedureType,
				"PROOF_OF_ADDRESS",
				"Justificante de domicilio",
				"Necesario si solicitas cambio de domicilio en el documento.",
				false,
				"""
				{"expectedDocumentType":"proof_of_address"}
				""");
		return save(procedureType, procedureTypeRepository, workflowDefinitionService);
	}

	static UUID seedTarjetaSanitaria(
			ProcedureTypeRepository procedureTypeRepository,
			WorkflowDefinitionService workflowDefinitionService) {
		ProcedureType procedureType = procedure(
				TARJETA_SANITARIA,
				"Alta en el sistema público de salud y expedición de la tarjeta sanitaria individual (TSI).",
				10);
		addRequirement(
				procedureType,
				"IDENTITY_DOCUMENT",
				"Documento de identidad",
				"DNI, NIE o pasaporte en vigor.",
				true,
				IDENTITY_RULES);
		addRequirement(
				procedureType,
				"EMPADRONAMIENTO_CERT",
				"Certificado de empadronamiento",
				"Acredita residencia en la comunidad autónoma donde solicitas la tarjeta.",
				true,
				"""
				{"expectedDocumentType":"proof_of_address"}
				""");
		return save(procedureType, procedureTypeRepository, workflowDefinitionService);
	}

	static UUID seedAltaAutonomo(
			ProcedureTypeRepository procedureTypeRepository,
			WorkflowDefinitionService workflowDefinitionService) {
		ProcedureType procedureType = procedure(
				ALTA_AUTONOMO,
				"Alta en el Régimen Especial de Trabajadores Autónomos (RETA) ante la Seguridad Social.",
				7);
		addRequirement(
				procedureType,
				"IDENTITY_DOCUMENT",
				"Documento de identidad",
				"DNI o NIE en vigor del futuro autónomo.",
				true,
				IDENTITY_RULES);
		addRequirement(
				procedureType,
				"TAX_CERTIFICATE",
				"Alta censal en Hacienda",
				"Modelo 036 o 037 de alta, modificación o baja en el censo de obligaciones tributarias.",
				true,
				"""
				{"expectedDocumentType":"tax_certificate"}
				""");
		addRequirement(
				procedureType,
				"PROOF_OF_ADDRESS",
				"Domicilio fiscal",
				"Justificante del domicilio fiscal declarado ante la AEAT.",
				false,
				"""
				{"expectedDocumentType":"proof_of_address"}
				""");
		return save(procedureType, procedureTypeRepository, workflowDefinitionService);
	}

	static UUID seedCambioTitularSuministros(
			ProcedureTypeRepository procedureTypeRepository,
			WorkflowDefinitionService workflowDefinitionService) {
		ProcedureType procedureType = procedure(
				CAMBIO_TITULAR_SUMINISTROS,
				"Cambio de titular del contrato de suministro eléctrico (o gas) al comprar, alquilar o heredar una vivienda.",
				5);
		addRequirement(
				procedureType,
				"IDENTITY_DOCUMENT",
				"Documento de identidad",
				"DNI o NIE en vigor del nuevo titular.",
				true,
				IDENTITY_RULES);
		addRequirement(
				procedureType,
				"PROOF_OF_ADDRESS",
				"Título sobre la vivienda",
				"Contrato de alquiler, escritura o autorización del propietario.",
				true,
				"""
				{"expectedDocumentType":"proof_of_address"}
				""");
		addRequirement(
				procedureType,
				"RENTAL_CONTRACT",
				"Contrato de alquiler",
				"Solo si el cambio se solicita como arrendatario.",
				false,
				"""
				{"expectedDocumentType":"rental_contract","requireFutureExpiration":true}
				""");
		return save(procedureType, procedureTypeRepository, workflowDefinitionService);
	}

	static UUID seedParejaDeHecho(
			ProcedureTypeRepository procedureTypeRepository,
			WorkflowDefinitionService workflowDefinitionService) {
		ProcedureType procedureType = procedure(
				PAREJA_DE_HECHO,
				"Inscripción de la pareja en el registro municipal o autonómico de parejas de hecho.",
				20);
		addRequirement(
				procedureType,
				"IDENTITY_DOCUMENT_A",
				"Documento de identidad (solicitante 1)",
				"DNI o NIE en vigor de la primera persona.",
				true,
				IDENTITY_RULES);
		addRequirement(
				procedureType,
				"IDENTITY_DOCUMENT_B",
				"Documento de identidad (solicitante 2)",
				"DNI o NIE en vigor de la segunda persona.",
				true,
				IDENTITY_RULES);
		addRequirement(
				procedureType,
				"COHABITATION_PROOF",
				"Prueba de convivencia",
				"Certificados de empadronamiento en el mismo domicilio u otro documento que acredite convivencia estable.",
				true,
				"""
				{"expectedDocumentType":"proof_of_address"}
				""");
		return save(procedureType, procedureTypeRepository, workflowDefinitionService);
	}

	static UUID seedGradoDependencia(
			ProcedureTypeRepository procedureTypeRepository,
			WorkflowDefinitionService workflowDefinitionService) {
		ProcedureType procedureType = procedure(
				GRADO_DEPENDENCIA,
				"Reconocimiento del grado de dependencia para acceder a servicios y prestaciones del Sistema para la Autonomía y Atención a la Dependencia.",
				90);
		addRequirement(
				procedureType,
				"IDENTITY_DOCUMENT",
				"Documento de identidad",
				"DNI o NIE de la persona solicitante o de quien la representa.",
				true,
				IDENTITY_RULES);
		addRequirement(
				procedureType,
				"PROOF_OF_ADDRESS",
				"Certificado de empadronamiento",
				"Domicilio habitual de la persona beneficiaria.",
				true,
				"""
				{"expectedDocumentType":"proof_of_address"}
				""");
		addRequirement(
				procedureType,
				"MEDICAL_REPORT",
				"Informe clínico",
				"Informe médico que describa el estado de salud y las limitaciones funcionales.",
				true,
				"""
				{"expectedDocumentType":"other","minConfidence":0.5}
				""");
		return save(procedureType, procedureTypeRepository, workflowDefinitionService);
	}

	static UUID seedBonificacionIbi(
			ProcedureTypeRepository procedureTypeRepository,
			WorkflowDefinitionService workflowDefinitionService) {
		ProcedureType procedureType = procedure(
				BONIFICACION_IBI,
				"Solicitud de bonificación o exención parcial del Impuesto sobre Bienes Inmuebles en vivienda habitual.",
				30);
		addRequirement(
				procedureType,
				"IDENTITY_DOCUMENT",
				"Documento de identidad",
				"DNI o NIE del titular o titulares del inmueble.",
				true,
				IDENTITY_RULES);
		addRequirement(
				procedureType,
				"PROOF_OF_ADDRESS",
				"Acreditación de vivienda habitual",
				"Certificado de empadronamiento en el municipio o escritura de la vivienda.",
				true,
				"""
				{"expectedDocumentType":"proof_of_address"}
				""");
		addRequirement(
				procedureType,
				"TAX_RECEIPT",
				"Recibo del IBI",
				"Último recibo o carta de pago del impuesto sobre el inmueble.",
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
