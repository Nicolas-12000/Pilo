package com.pilo.auth;

import com.pilo.users.Role;
import com.pilo.users.User;
import java.util.UUID;

public record UserResponse(UUID id, String email, String fullName, Role role) {

	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
	}
}
