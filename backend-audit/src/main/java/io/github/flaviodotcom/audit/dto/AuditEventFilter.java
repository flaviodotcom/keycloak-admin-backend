package io.github.flaviodotcom.audit.dto;

import java.time.Instant;

public record AuditEventFilter(
        String eventType,
        String correlationId,
        String actorId,
        String subjectType,
        String subjectId,
        String topic,
        Instant receivedFrom,
        Instant receivedTo
) {
}
