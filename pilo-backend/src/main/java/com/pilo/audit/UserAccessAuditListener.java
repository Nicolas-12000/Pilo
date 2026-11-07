package com.pilo.audit;

import com.pilo.users.User;
import com.pilo.users.UserRepository;
import com.pilo.users.events.UserAccessChangeEvent;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserAccessAuditListener {

	private final UserRepository userRepository;
	private final AuditService auditService;

	public UserAccessAuditListener(UserRepository userRepository, AuditService auditService) {
		this.userRepository = userRepository;
		this.auditService = auditService;
	}

	@EventListener
	@Transactional
	public void onUserAccessChange(UserAccessChangeEvent event) {
		User actor = userRepository.findById(event.actorId()).orElseThrow();
		UUID targetId = event.targetId();
		auditService.record(
				actor,
				event.enabled() ? "USER_ENABLED" : "USER_DISABLED",
				"user:" + targetId,
				"SUCCESS",
				Map.of("email", event.targetEmail()));
	}
}
