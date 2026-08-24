package com.pilo.audit;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.pilo.users.User;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

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

	private String toJson(Map<String, Object> metadata) {
		try {
			return objectMapper.writeValueAsString(metadata);
		} catch (JacksonException exception) {
			return "{}";
		}
	}
}
