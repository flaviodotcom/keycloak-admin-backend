package io.github.flaviodotcom.audit.service;

import io.github.flaviodotcom.audit.entity.AuditEvent;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;

@ApplicationScoped
public class AuditEventMapper {

    public AuditEvent toEntity(String topic, String payloadJson, AuditEventPayload payload) {
        var event = new AuditEvent();
        event.eventId = payload.requiredText("eventId");
        event.schemaVersion = payload.requiredInt("schemaVersion");
        event.topic = topic;
        event.eventType = payload.requiredText("eventType");
        event.source = payload.optionalText("source");
        event.actorId = payload.optionalNestedText("actor", "id");
        event.subjectType = payload.optionalNestedText("subject", "type");
        event.subjectId = payload.optionalNestedText("subject", "id");
        event.occurredAt = payload.optionalOffsetDateTime("occurredAt");
        event.receivedAt = OffsetDateTime.now();
        event.payloadJson = payloadJson;
        return event;
    }
}
