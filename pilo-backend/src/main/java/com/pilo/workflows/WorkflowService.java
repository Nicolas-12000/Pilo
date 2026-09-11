package com.pilo.workflows;

import com.pilo.audit.AuditService;
import com.pilo.cases.CaseAccessService;
import com.pilo.cases.CaseStatus;
import com.pilo.cases.ProcedureCase;
import com.pilo.cases.ProcedureCaseRepository;
import com.pilo.documents.Document;
import com.pilo.documents.DocumentRepository;
import com.pilo.documents.DocumentStatus;
import com.pilo.procedures.Requirement;
import com.pilo.users.Role;
import com.pilo.users.User;
import com.pilo.users.UserRepository;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

@Service
@Validated
public class WorkflowService {

	private static final Set<DocumentStatus> PRESENT_STATUSES =
			EnumSet.of(DocumentStatus.UPLOADED, DocumentStatus.PROCESSING, DocumentStatus.VALIDATED);

	private final WorkflowTaskRepository workflowTaskRepository;
	private final CaseAccessService caseAccessService;
	private final ProcedureCaseRepository procedureCaseRepository;
	private final DocumentRepository documentRepository;
	private final UserRepository userRepository;
	private final AuditService auditService;

	public WorkflowService(
			WorkflowTaskRepository workflowTaskRepository,
			CaseAccessService caseAccessService,
			ProcedureCaseRepository procedureCaseRepository,
			DocumentRepository documentRepository,
			UserRepository userRepository,
			AuditService auditService) {
		this.workflowTaskRepository = workflowTaskRepository;
		this.caseAccessService = caseAccessService;
		this.procedureCaseRepository = procedureCaseRepository;
		this.documentRepository = documentRepository;
		this.userRepository = userRepository;
		this.auditService = auditService;
	}

	@Transactional(readOnly = true)
	public WorkflowResponse getWorkflow(UUID caseId, UUID userId, Role role) {
		caseAccessService.requireAccessibleCase(caseId, userId, role);
		List<WorkflowTaskResponse> tasks = tasksFor(caseId).stream()
				.map(WorkflowTaskResponse::from)
				.toList();
		return new WorkflowResponse(caseId, tasks);
	}

	@Transactional
	public void completeFinalReview(UUID caseId, UUID reviewerId, Role role, @Valid FinalReviewRequest request) {
		if (role != Role.REVIEWER && role != Role.ADMIN) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		ProcedureCase procedureCase = caseAccessService.requireAccessibleCase(caseId, reviewerId, role);
		if (procedureCase.getStatus() != CaseStatus.UNDER_REVIEW) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CASE_NOT_READY_FOR_REVIEW");
		}

		WorkflowTask finalReview = tasksFor(caseId).stream()
				.filter(task -> "FINAL_REVIEW".equals(task.getTaskCode()))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "FINAL_REVIEW_NOT_FOUND"));

		if (finalReview.getStatus() != WorkflowTaskStatus.IN_PROGRESS) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "FINAL_REVIEW_NOT_ACTIVE");
		}

		User reviewer = userRepository
				.findById(reviewerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

		finalReview.setStatus(WorkflowTaskStatus.COMPLETED);
		procedureCase.setStatus(
				request.decision() == FinalReviewRequest.Decision.APPROVED ? CaseStatus.APPROVED : CaseStatus.REJECTED);
		if (request.decision() == FinalReviewRequest.Decision.APPROVED) {
			procedureCase.setProgressPercentage(100);
		}

		workflowTaskRepository.save(finalReview);
		procedureCaseRepository.save(procedureCase);

		Map<String, Object> metadata = new java.util.HashMap<>();
		metadata.put("decision", request.decision().name());
		if (request.comment() != null && !request.comment().isBlank()) {
			metadata.put("comment", request.comment().trim());
		}

		auditService.record(
				reviewer,
				"CASE_REVIEWED",
				"case:" + procedureCase.getId(),
				request.decision() == FinalReviewRequest.Decision.APPROVED ? "SUCCESS" : "FAILURE",
				metadata);
	}

	@Transactional
	public void reevaluate(UUID caseId) {
		ProcedureCase procedureCase = procedureCaseRepository
				.findDetailedById(caseId)
				.orElseThrow();
		if (procedureCase.getStatus() == CaseStatus.APPROVED || procedureCase.getStatus() == CaseStatus.REJECTED) {
			return;
		}

		List<Document> documents = documentRepository.findByCaseId(caseId);
		List<Requirement> mandatoryRequirements = procedureCase.getProcedureType().getRequirements().stream()
				.filter(Requirement::isMandatory)
				.toList();

		boolean allMandatoryPresent = allMandatoryMatch(mandatoryRequirements, documents, PRESENT_STATUSES);
		boolean allMandatoryValidated =
				allMandatoryMatch(mandatoryRequirements, documents, EnumSet.of(DocumentStatus.VALIDATED));
		long validatedMandatory = mandatoryRequirements.stream()
				.filter(requirement -> hasStatus(requirement.getId(), documents, EnumSet.of(DocumentStatus.VALIDATED)))
				.count();

		int progress = mandatoryRequirements.isEmpty()
				? 100
				: (int) ((validatedMandatory * 100) / mandatoryRequirements.size());
		procedureCase.setProgressPercentage(progress);

		List<WorkflowTask> tasks = tasksFor(caseId);
		Map<String, WorkflowTask> tasksByCode = tasks.stream()
				.collect(Collectors.toMap(WorkflowTask::getTaskCode, Function.identity()));

		for (WorkflowTask task : tasks) {
			if (task.getCompletionRule() == CompletionRule.ON_CREATE) {
				task.setStatus(WorkflowTaskStatus.COMPLETED);
				continue;
			}

			boolean dependencyCompleted = isDependencyCompleted(task, tasksByCode);
			if (!dependencyCompleted) {
				task.setStatus(WorkflowTaskStatus.BLOCKED);
				continue;
			}

			task.setStatus(statusFor(task, allMandatoryPresent, allMandatoryValidated));
		}

		if (allMandatoryValidated && !mandatoryRequirements.isEmpty()) {
			procedureCase.setStatus(CaseStatus.UNDER_REVIEW);
		}

		procedureCaseRepository.save(procedureCase);
		workflowTaskRepository.saveAll(tasks);
	}

	private List<WorkflowTask> tasksFor(UUID caseId) {
		return workflowTaskRepository.findByCaseId(caseId).stream()
				.collect(Collectors.toMap(
						WorkflowTask::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new))
				.values()
				.stream()
				.sorted(Comparator.comparingInt(WorkflowTask::getSortOrder))
				.toList();
	}

	private static WorkflowTaskStatus statusFor(
			WorkflowTask task, boolean allMandatoryPresent, boolean allMandatoryValidated) {
		return switch (task.getCompletionRule()) {
			case ON_CREATE -> WorkflowTaskStatus.COMPLETED;
			case ALL_MANDATORY_PRESENT -> allMandatoryPresent
					? WorkflowTaskStatus.COMPLETED
					: WorkflowTaskStatus.IN_PROGRESS;
			case ALL_MANDATORY_VALIDATED -> allMandatoryValidated
					? WorkflowTaskStatus.COMPLETED
					: WorkflowTaskStatus.IN_PROGRESS;
			case MANUAL -> task.getStatus() == WorkflowTaskStatus.COMPLETED
					? WorkflowTaskStatus.COMPLETED
					: WorkflowTaskStatus.IN_PROGRESS;
		};
	}

	private static boolean isDependencyCompleted(WorkflowTask task, Map<String, WorkflowTask> tasksByCode) {
		if (task.getDependsOnTask() == null) {
			return true;
		}
		WorkflowTask dependency = tasksByCode.get(task.getDependsOnTask().getTaskCode());
		return dependency != null && dependency.getStatus() == WorkflowTaskStatus.COMPLETED;
	}

	private static boolean allMandatoryMatch(
			List<Requirement> mandatoryRequirements, List<Document> documents, Set<DocumentStatus> statuses) {
		if (mandatoryRequirements.isEmpty()) {
			return true;
		}
		return mandatoryRequirements.stream()
				.allMatch(requirement -> hasStatus(requirement.getId(), documents, statuses));
	}

	private static boolean hasStatus(UUID requirementId, List<Document> documents, Set<DocumentStatus> statuses) {
		return documents.stream()
				.anyMatch(document -> document.getRequirement().getId().equals(requirementId)
						&& statuses.contains(document.getStatus()));
	}
}
