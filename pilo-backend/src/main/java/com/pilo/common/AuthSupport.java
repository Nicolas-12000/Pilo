package com.pilo.common;

import com.pilo.users.Role;
import java.util.UUID;
import org.springframework.security.core.Authentication;

public final class AuthSupport {

	private AuthSupport() {}

	public static UUID userId(Authentication authentication) {
		return UUID.fromString(authentication.getName());
	}

	public static Role role(Authentication authentication) {
		return Role.valueOf(authentication.getAuthorities().stream()
				.map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
				.findFirst()
				.orElseThrow());
	}
}
