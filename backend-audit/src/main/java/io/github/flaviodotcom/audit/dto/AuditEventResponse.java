package io.github.flaviodotcom.audit.dto;

import io.github.flaviodotcom.audit.entities.AuditEvent;

import java.time.OffsetDateTime;

public record AuditEventResponse(
        String eventId,
        int schemaVersion,
        String topic,
        String eventType,
        String source,
        String correlationId,
        String actorId,
        String subjectType,
        String subjectId,
        OffsetDateTime occurredAt,
        OffsetDateTime receivedAt,
        String payloadJson
) {

    public static AuditEventResponse fromEntity(AuditEvent event) {
        return new AuditEventResponse(
                event.eventId,
                event.schemaVersion,
                event.topic,
                event.eventType,
                event.source,
                event.correlationId,
                event.actorId,
                event.subjectType,
                event.subjectId,
                event.occurredAt,
                event.receivedAt,
                event.payloadJson
        );
    }
}
