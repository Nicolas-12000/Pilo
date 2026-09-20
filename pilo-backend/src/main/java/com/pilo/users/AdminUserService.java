package com.pilo.users;

import com.pilo.audit.AuditService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminUserService {

	private final UserRepository userRepository;
	private final AuditService auditService;

	public AdminUserService(UserRepository userRepository, AuditService auditService) {
		this.userRepository = userRepository;
		this.auditService = auditService;
	}

	@Transactional(readOnly = true)
	public List<AdminUserResponse> list() {
		return userRepository.findAllByOrderByEmailAsc().stream().map(AdminUserResponse::from).toList();
	}

	@Transactional
	public AdminUserResponse setEnabled(UUID actorId, UUID targetId, boolean enabled) {
		if (actorId.equals(targetId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CANNOT_DISABLE_SELF");
		}
		User actor = userRepository
				.findById(actorId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
		User target = userRepository
				.findById(targetId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));
		if (!enabled && target.getRole() == Role.ADMIN && userRepository.countByRoleAndEnabledTrue(Role.ADMIN) <= 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "LAST_ADMIN");
		}
		target.setEnabled(enabled);
		target.bumpTokenVersion();
		userRepository.save(target);
		auditService.record(
				actor,
				enabled ? "USER_ENABLED" : "USER_DISABLED",
				"user:" + target.getId(),
				"SUCCESS",
				Map.of("email", target.getEmail()));
		return AdminUserResponse.from(target);
	}
}
