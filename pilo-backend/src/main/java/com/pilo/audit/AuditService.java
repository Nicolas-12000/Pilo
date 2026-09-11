package com.pilo.audit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.pilo.users.User;
import java.util.Map;

@Service
public class AuditService {

	private static final int CASE_TIMELINE_LIMIT = 20;

	private final AuditEventRepository auditEventRepository;
	private final ObjectMapper objectMapper;

	public AuditService(AuditEventRepository auditEventRepository, ObjectMapper objectMapper) {
		this.auditEventRepository = auditEventRepository;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public void record(User actor, String action, String resource, String result, Map<String, Object> metadata) {
		AuditEvent event = new AuditEvent();
		event.setActor(actor);
		event.setAction(action);
		event.setResource(resource);
		event.setResult(result);
		event.setMetadata(toJson(metadata));
		auditEventRepository.save(event);
	}

	@Transactional(readOnly = true)
	public List<AuditEventResponse> getCaseTimeline(UUID caseId, List<UUID> documentIds) {
		List<String> resources = new ArrayList<>(documentIds.size() + 1);
		resources.add("case:" + caseId);
		documentIds.forEach(documentId -> resources.add("document:" + documentId));
		return auditEventRepository
				.findByResources(resources, PageRequest.of(0, CASE_TIMELINE_LIMIT))
				.stream()
				.map(AuditEventResponse::from)
				.toList();
	}

	private String toJson(Map<String, Object> metadata) {
		try {
			return objectMapper.writeValueAsString(metadata);
		} catch (JacksonException exception) {
			return "{}";
		}
	}
}
