package com.pilo.users;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

	private final AdminUserService adminUserService;

	public AdminUserController(AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}

	@GetMapping
	public List<AdminUserResponse> list() {
		return adminUserService.list();
	}

	@PostMapping("/{id}/enabled")
	public AdminUserResponse setEnabled(
			@PathVariable UUID id,
			@Valid @RequestBody SetEnabledRequest request,
			Authentication authentication) {
		return adminUserService.setEnabled(UUID.fromString(authentication.getName()), id, request.enabled());
	}
}
