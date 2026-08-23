package com.pilo.auth;

import com.pilo.users.Role;
import java.util.UUID;

public record JwtPrincipal(UUID userId, String email, Role role) {
}
