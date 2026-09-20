package com.pilo.users;

import java.util.UUID;

public record AdminUserResponse(UUID id, String email, String fullName, Role role, boolean enabled) {

	public static AdminUserResponse from(User user) {
		return new AdminUserResponse(
				user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.isEnabled());
	}
}
