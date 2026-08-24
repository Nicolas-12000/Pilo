package com.pilo.workflows;

import static org.assertj.core.api.Assertions.assertThat;

import com.pilo.cases.CaseService;
import com.pilo.cases.CreateCaseRequest;
import com.pilo.procedures.ProcedureType;
import com.pilo.procedures.ProcedureTypeRepository;
import com.pilo.procedures.Requirement;
import com.pilo.support.AbstractIntegrationTest;
import com.pilo.users.Role;
import com.pilo.users.User;
import com.pilo.users.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class CaseWorkflowIT extends AbstractIntegrationTest {

	@Autowired
	private ProcedureTypeRepository procedureTypeRepository;

	@Autowired
	private WorkflowDefinitionService workflowDefinitionService;

	@Autowired
	private CaseService caseService;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void creatingACaseMaterializesConfiguredWorkflowSteps() {
		User user = persistUser();
		ProcedureType procedureType = persistProcedureType();
		workflowDefinitionService.attach(procedureType, WorkflowBlueprints.documentCollectionReview());

		UUID caseId = caseService.createCase(user.getId(), new CreateCaseRequest(procedureType.getId())).id();
		List<WorkflowTaskResponse> tasks = workflowService.getWorkflow(caseId, user.getId(), Role.USER).tasks();

		assertThat(tasks)
				.extracting(WorkflowTaskResponse::taskCode)
				.containsExactly("OPEN_CASE", "COLLECT_DOCUMENTS", "VALIDATE_DOCUMENTS", "FINAL_REVIEW");
		assertThat(tasks.get(0).status()).isEqualTo(WorkflowTaskStatus.COMPLETED);
		assertThat(tasks.get(1).status()).isEqualTo(WorkflowTaskStatus.IN_PROGRESS);
		assertThat(tasks.get(2).status()).isEqualTo(WorkflowTaskStatus.BLOCKED);
		assertThat(tasks.get(3).status()).isEqualTo(WorkflowTaskStatus.BLOCKED);
	}

	@Test
	void attachingTheSameBlueprintTwiceIsIdempotent() {
		ProcedureType procedureType = persistProcedureType();
		workflowDefinitionService.attach(procedureType, WorkflowBlueprints.documentCollectionReview());
		workflowDefinitionService.attach(procedureType, WorkflowBlueprints.documentCollectionReview());

		assertThat(workflowDefinitionService.requireDefinitions(procedureType)).hasSize(4);
	}

	private User persistUser() {
		User user = new User();
		user.setEmail("workflow@pilo.test");
		user.setFullName("Workflow Tester");
		user.setRole(Role.USER);
		user.setPasswordHash(passwordEncoder.encode("Password123!"));
		return userRepository.save(user);
	}

	private ProcedureType persistProcedureType() {
		ProcedureType procedureType = new ProcedureType();
		procedureType.setTitle("Trámite de prueba");
		procedureType.setDescription("Usado para verificar el workflow configurable.");
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
