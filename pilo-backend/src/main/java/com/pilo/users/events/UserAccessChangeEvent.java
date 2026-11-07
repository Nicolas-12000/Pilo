package com.pilo.users.events;

import java.util.UUID;

/**
 * Published when an admin enables or disables a user account. Keeps {@code com.pilo.users} free of
 * direct dependencies on auditing — listeners in {@code com.pilo.audit} record the trail.
 */
public record UserAccessChangeEvent(
		UUID actorId, UUID targetId, String targetEmail, boolean enabled) {}
