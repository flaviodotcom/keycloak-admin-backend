package io.github.flaviodotcom.service.events;

import java.time.OffsetDateTime;
import java.util.Map;

public record IdentityEvent(
        String eventId,
        int schemaVersion,
        String eventType,
        String source,
        String correlationId,
        Actor actor,
        Subject subject,
        OffsetDateTime occurredAt,
        Map<String, Object> data
) {

    public record Actor(String id) {
    }

    public record Subject(String type, String id) {
    }
}
