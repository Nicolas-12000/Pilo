package com.pilo.procedures;

import com.pilo.audit.AuditService;
import com.pilo.cases.ProcedureCaseRepository;
import com.pilo.common.CacheNames;
import com.pilo.documents.DocumentRepository;
import com.pilo.documents.validation.ValidationRulesParser;
import com.pilo.users.User;
import com.pilo.users.UserRepository;
import com.pilo.workflows.WorkflowBlueprints;
import com.pilo.workflows.WorkflowDefinitionService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminProcedureCatalogService {

	private final ProcedureTypeRepository procedureTypeRepository;
	private final RequirementRepository requirementRepository;
	private final ProcedureCaseRepository procedureCaseRepository;
	private final DocumentRepository documentRepository;
	private final WorkflowDefinitionService workflowDefinitionService;
	private final ValidationRulesParser validationRulesParser;
	private final AuditService auditService;
	private final UserRepository userRepository;

	public AdminProcedureCatalogService(
			ProcedureTypeRepository procedureTypeRepository,
			RequirementRepository requirementRepository,
			ProcedureCaseRepository procedureCaseRepository,
			DocumentRepository documentRepository,
			WorkflowDefinitionService workflowDefinitionService,
			ValidationRulesParser validationRulesParser,
			AuditService auditService,
			UserRepository userRepository) {
		this.procedureTypeRepository = procedureTypeRepository;
		this.requirementRepository = requirementRepository;
		this.procedureCaseRepository = procedureCaseRepository;
		this.documentRepository = documentRepository;
		this.workflowDefinitionService = workflowDefinitionService;
		this.validationRulesParser = validationRulesParser;
		this.auditService = auditService;
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public List<ProcedureTypeSummaryResponse> listAll() {
		return procedureTypeRepository.findAllWithRequirements().stream()
				.map(ProcedureTypeSummaryResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public AdminProcedureTypeDetailResponse getById(UUID id) {
		ProcedureType procedureType = requireProcedureType(id);
		return AdminProcedureTypeDetailResponse.from(
				procedureType, procedureCaseRepository.existsByProcedureTypeId(id), validationRulesParser);
	}

	@CacheEvict(cacheNames = CacheNames.PROCEDURE_CATALOG, allEntries = true)
	@Transactional
	public AdminProcedureTypeDetailResponse create(UUID actorId, CreateProcedureTypeRequest request) {
		User actor = requireUser(actorId);
		ProcedureType procedureType = new ProcedureType();
		procedureType.setTitle(request.title().trim());
		procedureType.setDescription(request.description().trim());
		procedureType.setTargetDays(request.targetDays());
		procedureTypeRepository.save(procedureType);
		workflowDefinitionService.attach(procedureType, WorkflowBlueprints.documentCollectionReview());

		audit(actor, "PROCEDURE_TYPE_CREATED", procedureType.getId(), Map.of("title", procedureType.getTitle()));
		return getById(procedureType.getId());
	}

	@CacheEvict(cacheNames = CacheNames.PROCEDURE_CATALOG, allEntries = true)
	@Transactional
	public AdminProcedureTypeDetailResponse update(UUID actorId, UUID id, UpdateProcedureTypeRequest request) {
		User actor = requireUser(actorId);
		ProcedureType procedureType = requireProcedureType(id);
		procedureType.setTitle(request.title().trim());
		procedureType.setDescription(request.description().trim());
		procedureType.setTargetDays(request.targetDays());
		procedureTypeRepository.save(procedureType);

		audit(actor, "PROCEDURE_TYPE_UPDATED", procedureType.getId(), Map.of("title", procedureType.getTitle()));
		return getById(id);
	}

	@CacheEvict(cacheNames = CacheNames.PROCEDURE_CATALOG, allEntries = true)
	@Transactional
	public AdminRequirementResponse addRequirement(UUID actorId, UUID procedureTypeId, CreateRequirementRequest request) {
		User actor = requireUser(actorId);
		ProcedureType procedureType = requireProcedureType(procedureTypeId);
		String code = request.code().trim().toUpperCase();
		if (requirementRepository.existsByProcedureTypeIdAndCode(procedureTypeId, code)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "REQUIREMENT_CODE_EXISTS");
		}

		Requirement requirement = new Requirement();
		requirement.setProcedureType(procedureType);
		requirement.setCode(code);
		requirement.setName(request.name().trim());
		requirement.setDescription(request.description().trim());
		requirement.setMandatory(request.mandatory());
		requirement.setValidationRules(serializeRules(request.validationRules()));
		requirementRepository.save(requirement);

		audit(actor, "REQUIREMENT_CREATED", procedureTypeId, Map.of("code", requirement.getCode()));
		return AdminRequirementResponse.from(requirement, validationRulesParser);
	}

	@CacheEvict(cacheNames = CacheNames.PROCEDURE_CATALOG, allEntries = true)
	@Transactional
	public AdminRequirementResponse updateRequirement(
			UUID actorId, UUID procedureTypeId, UUID requirementId, UpdateRequirementRequest request) {
		User actor = requireUser(actorId);
		Requirement requirement = requireRequirement(procedureTypeId, requirementId);
		requirement.setName(request.name().trim());
		requirement.setDescription(request.description().trim());
		requirement.setMandatory(request.mandatory());
		requirement.setValidationRules(serializeRules(request.validationRules()));
		requirementRepository.save(requirement);

		audit(actor, "REQUIREMENT_UPDATED", procedureTypeId, Map.of("code", requirement.getCode()));
		return AdminRequirementResponse.from(requirement, validationRulesParser);
	}

	@CacheEvict(cacheNames = CacheNames.PROCEDURE_CATALOG, allEntries = true)
	@Transactional
	public void deleteRequirement(UUID actorId, UUID procedureTypeId, UUID requirementId) {
		User actor = requireUser(actorId);
		Requirement requirement = requireRequirement(procedureTypeId, requirementId);
		if (documentRepository.existsByRequirementId(requirementId)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "REQUIREMENT_IN_USE");
		}
		requirementRepository.delete(requirement);
		audit(actor, "REQUIREMENT_DELETED", procedureTypeId, Map.of("code", requirement.getCode()));
	}

	private ProcedureType requireProcedureType(UUID id) {
		return procedureTypeRepository
				.findByIdWithRequirements(id)
				.orElseThrow(ProcedureTypeNotFoundException::new);
	}

	private Requirement requireRequirement(UUID procedureTypeId, UUID requirementId) {
		Requirement requirement = requirementRepository
				.findDetailedById(requirementId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "REQUIREMENT_NOT_FOUND"));
		if (!requirement.getProcedureType().getId().equals(procedureTypeId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "REQUIREMENT_MISMATCH");
		}
		return requirement;
	}

	private User requireUser(UUID actorId) {
		return userRepository
				.findById(actorId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
	}

	private String serializeRules(AdminValidationRulesRequest rules) {
		return validationRulesParser.serialize(new ValidationRulesParser.ValidationRulesRequest(
				rules.expectedDocumentType().trim(),
				rules.requireFutureExpiration(),
				rules.minConfidence()));
	}

	private void audit(User actor, String action, UUID procedureTypeId, Map<String, Object> metadata) {
		auditService.record(actor, action, "procedure-type:" + procedureTypeId, "SUCCESS", metadata);
	}
}
