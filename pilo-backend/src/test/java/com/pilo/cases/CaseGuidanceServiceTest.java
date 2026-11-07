package com.pilo.cases;

import static org.assertj.core.api.Assertions.assertThat;

import com.pilo.documents.Document;
import com.pilo.documents.DocumentStatus;
import com.pilo.procedures.Requirement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CaseGuidanceServiceTest {

	private final CaseGuidanceService service = new CaseGuidanceService();

	@Test
	void rejectedMandatoryDocumentExplainsHumanReviewToTheCitizen() {
		Requirement requirement = mandatory("DNI");
		Document document = document(requirement, DocumentStatus.REJECTED);
		ProcedureCase procedureCase = openCase(CaseStatus.IN_PROGRESS, 0);

		var guidance = service.summarize(procedureCase, List.of(requirement), List.of(document));

		assertThat(guidance.citizenSummary()).contains("validación automática");
		assertThat(guidance.reviewerSummary()).contains("1 rechazados");
		assertThat(service.briefForReviewQueue(procedureCase, List.of(requirement), List.of(document)))
				.contains("falso negativo");
	}

	@Test
	void pendingUploadStaysPendingUntilAFileExists() {
		Requirement requirement = mandatory("Empadronamiento");
		ProcedureCase procedureCase = openCase(CaseStatus.IN_PROGRESS, 0);

		var guidance = service.summarize(procedureCase, List.of(requirement), List.of());

		assertThat(guidance.citizenSummary()).contains("Queda subir la documentación");
	}

	private static ProcedureCase openCase(CaseStatus status, int progress) {
		ProcedureCase procedureCase = new ProcedureCase();
		procedureCase.setStatus(status);
		procedureCase.setProgressPercentage(progress);
		procedureCase.setDeadlineAt(Instant.now().plus(4, ChronoUnit.DAYS));
		return procedureCase;
	}

	private static Requirement mandatory(String name) {
		Requirement requirement = new Requirement();
		requirement.setId(UUID.randomUUID());
		requirement.setCode(name);
		requirement.setName(name);
		requirement.setDescription("Documento oficial.");
		requirement.setMandatory(true);
		return requirement;
	}

	private static Document document(Requirement requirement, DocumentStatus status) {
		Document document = new Document();
		document.setRequirement(requirement);
		document.setStatus(status);
		return document;
	}
}
