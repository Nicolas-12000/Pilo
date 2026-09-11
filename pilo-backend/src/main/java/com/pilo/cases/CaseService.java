package com.pilo.cases;

import com.pilo.audit.AuditEventResponse;
import com.pilo.audit.AuditService;
import com.pilo.documents.Document;
import com.pilo.documents.DocumentRepository;
import com.pilo.procedures.ProcedureType;
import com.pilo.procedures.ProcedureTypeRepository;
import com.pilo.users.Role;
import com.pilo.users.User;
import com.pilo.users.UserRepository;
import com.pilo.workflows.WorkflowInitializer;
import com.pilo.workflows.WorkflowService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CaseService {

	private final ProcedureCaseRepository procedureCaseRepository;
	private final ProcedureTypeRepository procedureTypeRepository;
	private final UserRepository userRepository;
	private final DocumentRepository documentRepository;
	private final CaseNumberGenerator caseNumberGenerator;
	private final CaseAccessService caseAccessService;
	private final WorkflowInitializer workflowInitializer;
	private final WorkflowService workflowService;
	private final AuditService auditService;

	public CaseService(
			ProcedureCaseRepository procedureCaseRepository,
			ProcedureTypeRepository procedureTypeRepository,
			UserRepository userRepository,
			DocumentRepository documentRepository,
			CaseNumberGenerator caseNumberGenerator,
			CaseAccessService caseAccessService,
			WorkflowInitializer workflowInitializer,
			WorkflowService workflowService,
			AuditService auditService) {
		this.procedureCaseRepository = procedureCaseRepository;
		this.procedureTypeRepository = procedureTypeRepository;
		this.userRepository = userRepository;
		this.documentRepository = documentRepository;
		this.caseNumberGenerator = caseNumberGenerator;
		this.caseAccessService = caseAccessService;
		this.workflowInitializer = workflowInitializer;
		this.workflowService = workflowService;
		this.auditService = auditService;
	}

	@Transactional
	public CaseDetailResponse createCase(UUID userId, CreateCaseRequest request) {
		User user = userRepository
				.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
		ProcedureType procedureType = procedureTypeRepository
				.findByIdWithRequirements(request.procedureTypeId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PROCEDURE_TYPE_NOT_FOUND"));

		ProcedureCase procedureCase = new ProcedureCase();
		procedureCase.setCaseNumber(caseNumberGenerator.nextCaseNumber());
		procedureCase.setUser(user);
		procedureCase.setProcedureType(procedureType);
		procedureCase.setStatus(CaseStatus.IN_PROGRESS);
		procedureCase.setProgressPercentage(0);
		procedureCase.setDeadlineAt(Instant.now().plus(procedureType.getTargetDays(), ChronoUnit.DAYS));
		procedureCaseRepository.save(procedureCase);

		workflowInitializer.initializeFor(procedureCase);
		workflowService.reevaluate(procedureCase.getId());
		auditService.record(
				user,
				"CASE_CREATED",
				"case:" + procedureCase.getId(),
				"SUCCESS",
				Map.of("caseNumber", procedureCase.getCaseNumber(), "procedureTypeId", procedureType.getId().toString()));

		return getCaseDetail(procedureCase.getId(), userId, Role.USER);
	}

	@Transactional(readOnly = true)
	public List<CaseSummaryResponse> listMyCases(UUID userId) {
		return procedureCaseRepository.findByUserId(userId).stream()
				.map(CaseSummaryResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<PendingReviewCaseResponse> listPendingReview(Role role) {
		if (role != Role.REVIEWER && role != Role.ADMIN) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		return procedureCaseRepository.findPendingReview().stream()
				.map(PendingReviewCaseResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public CaseDetailResponse getCaseDetail(UUID caseId, UUID userId, Role role) {
		ProcedureCase procedureCase = caseAccessService.requireAccessibleCase(caseId, userId, role);
		List<Document> documents = documentRepository.findByCaseId(caseId);
		List<CaseRequirementStatusResponse> requirements = procedureCase.getProcedureType().getRequirements().stream()
				.map(requirement -> new CaseRequirementStatusResponse(
						requirement.getId(),
						requirement.getCode(),
						requirement.getName(),
						requirement.getDescription(),
						requirement.isMandatory(),
						resolveFulfillment(requirement.getId(), documents)))
				.toList();
		List<UUID> documentIds = documents.stream().map(Document::getId).toList();
		List<AuditEventResponse> auditEvents = auditService.getCaseTimeline(caseId, documentIds);

		return new CaseDetailResponse(
				procedureCase.getId(),
				procedureCase.getCaseNumber(),
				procedureCase.getProcedureType().getId(),
				procedureCase.getProcedureType().getTitle(),
				procedureCase.getProcedureType().getDescription(),
				procedureCase.getStatus(),
				procedureCase.getProgressPercentage(),
				procedureCase.getDeadlineAt(),
				procedureCase.getCreatedAt(),
				requirements,
				auditEvents);
	}

	private String resolveFulfillment(UUID requirementId, List<Document> documents) {
		return documents.stream()
				.filter(document -> document.getRequirement().getId().equals(requirementId))
				.findFirst()
				.map(document -> switch (document.getStatus()) {
					case VALIDATED -> "FULFILLED";
					case REJECTED, PROCESSING_FAILED -> "REJECTED";
					case PROCESSING, PENDING_UPLOAD -> "PROCESSING";
					default -> "UPLOADED";
				})
				.orElse("PENDING");
	}
}
