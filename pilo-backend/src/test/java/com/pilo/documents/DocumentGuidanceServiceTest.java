package com.pilo.documents;

import static org.assertj.core.api.Assertions.assertThat;

import com.pilo.ai.StructuredExtraction;
import com.pilo.documents.validation.ValidationResult;
import com.pilo.procedures.ProcedureType;
import com.pilo.procedures.Requirement;
import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentGuidanceServiceTest {

	private final DocumentGuidanceService service = new DocumentGuidanceService();

	@Test
	void rejectedGuidanceMentionsSubsanacionAndHumanReview() {
		Requirement requirement = requirement("Documento de identidad");
		var extraction = new StructuredExtraction("other", 0.42, "{}");
		var guidance =
				service.build(requirement, extraction, ValidationResult.failure(List.of("EXPECTED_DOCUMENT_TYPE")));

		assertThat(guidance.citizenGuidance()).contains("No hemos podido validar");
		assertThat(guidance.citizenGuidance()).contains("revisor humano");
		assertThat(guidance.citizenGuidance()).doesNotContain("El tipo de documento detectado");
		assertThat(guidance.reviewerSummary()).contains("El tipo de documento detectado no coincide");
	}

	private static Requirement requirement(String name) {
		ProcedureType procedureType = new ProcedureType();
		procedureType.setTitle("Demo");
		Requirement requirement = new Requirement();
		requirement.setProcedureType(procedureType);
		requirement.setCode("IDENTITY");
		requirement.setName(name);
		requirement.setDescription("DNI o NIE en vigor, anverso legible.");
		requirement.setMandatory(true);
		return requirement;
	}
}
