package com.pilo.audit;

import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(
		UUID id, String action, String resource, String result, String metadata, Instant timestamp) {

	public static AuditEventResponse from(AuditEvent event) {
		return new AuditEventResponse(
				event.getId(),
				event.getAction(),
				event.getResource(),
				event.getResult(),
				event.getMetadata(),
				event.getTimestamp());
	}
}
