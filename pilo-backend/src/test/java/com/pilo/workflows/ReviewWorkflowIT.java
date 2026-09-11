package com.pilo.workflows;

import static org.assertj.core.api.Assertions.assertThat;

import com.pilo.cases.CaseService;
import com.pilo.cases.CaseStatus;
import com.pilo.cases.CreateCaseRequest;
import com.pilo.cases.ProcedureCaseRepository;
import com.pilo.documents.Document;
import com.pilo.documents.DocumentRepository;
import com.pilo.documents.DocumentStatus;
import com.pilo.procedures.ProcedureType;
import com.pilo.procedures.ProcedureTypeRepository;
import com.pilo.procedures.Requirement;
import com.pilo.support.AbstractIntegrationTest;
import com.pilo.users.Role;
import com.pilo.users.User;
import com.pilo.users.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class ReviewWorkflowIT extends AbstractIntegrationTest {

	@Autowired
	private ProcedureTypeRepository procedureTypeRepository;

	@Autowired
	private WorkflowDefinitionService workflowDefinitionService;

	@Autowired
	private CaseService caseService;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private ProcedureCaseRepository procedureCaseRepository;

	@Autowired
	private DocumentRepository documentRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void completingFinalReviewApprovesCase() {
		User citizen = persistUser("citizen@pilo.test", Role.USER);
		User reviewer = persistUser("reviewer-flow@pilo.test", Role.REVIEWER);
		ProcedureType procedureType = persistProcedureType();
		workflowDefinitionService.attach(procedureType, WorkflowBlueprints.documentCollectionReview());

		UUID caseId = caseService.createCase(citizen.getId(), new CreateCaseRequest(procedureType.getId())).id();
		Requirement requirement = procedureType.getRequirements().getFirst();

		Document document = new Document();
		document.setProcedureCase(procedureCaseRepository.findDetailedById(caseId).orElseThrow());
		document.setRequirement(requirement);
		document.setFileName("identity.pdf");
		document.setMimeType("application/pdf");
		document.setFileSize(1024);
		document.setStorageKey("demo/identity.pdf");
		document.setStatus(DocumentStatus.VALIDATED);
		documentRepository.save(document);

		workflowService.reevaluate(caseId);

		assertThat(procedureCaseRepository.findDetailedById(caseId).orElseThrow().getStatus())
				.isEqualTo(CaseStatus.UNDER_REVIEW);

		workflowService.completeFinalReview(
				caseId,
				reviewer.getId(),
				Role.REVIEWER,
				new FinalReviewRequest(FinalReviewRequest.Decision.APPROVED, "Documentación correcta"));

		assertThat(procedureCaseRepository.findDetailedById(caseId).orElseThrow().getStatus())
				.isEqualTo(CaseStatus.APPROVED);
		assertThat(workflowService.getWorkflow(caseId, reviewer.getId(), Role.REVIEWER).tasks().stream()
						.filter(task -> "FINAL_REVIEW".equals(task.taskCode()))
						.findFirst()
						.orElseThrow()
						.status())
				.isEqualTo(WorkflowTaskStatus.COMPLETED);
	}

	private User persistUser(String email, Role role) {
		User user = new User();
		user.setEmail(email);
		user.setFullName("Workflow Tester");
		user.setRole(role);
		user.setPasswordHash(passwordEncoder.encode("Password123!"));
		return userRepository.save(user);
	}

	private ProcedureType persistProcedureType() {
		ProcedureType procedureType = new ProcedureType();
		procedureType.setTitle("Trámite de revisión");
		procedureType.setDescription("Usado para verificar la revisión final.");
		procedureType.setTargetDays(7);
		Requirement requirement = new Requirement();
		requirement.setProcedureType(procedureType);
		requirement.setCode("IDENTITY_DOCUMENT");
		requirement.setName("Documento de identidad");
		requirement.setDescription("Identificación oficial.");
		requirement.setMandatory(true);
		requirement.setValidationRules("{\"expectedDocumentType\":\"identity_document\"}");
		procedureType.getRequirements().add(requirement);
		return procedureTypeRepository.save(procedureType);
	}
}
