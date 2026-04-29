package io.github.flaviodotcom.notification.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record NotificationEvent(
        String eventId,
        int schemaVersion,
        String eventType,
        String source,
        String commandId,
        Actor actor,
        List<String> recipients,
        OffsetDateTime occurredAt,
        String errorMessage,
        Map<String, String> metadata
) {

    public record Actor(String id) {
    }
}
