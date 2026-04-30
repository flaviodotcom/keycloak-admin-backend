package io.github.flaviodotcom.notification.domain.factory;

import io.github.flaviodotcom.notification.dto.EmailNotificationCommand;
import io.github.flaviodotcom.notification.dto.NotificationEvent;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class NotificationEventFactory {

    private static final String SOURCE = "backend-notification";
    private static final int SCHEMA_VERSION = 1;

    public NotificationEvent sent(EmailNotificationCommand command) {
        return this.create(command, "notification.email.sent", null);
    }

    public NotificationEvent failed(EmailNotificationCommand command, String errorMessage) {
        return this.create(command, "notification.email.failed", errorMessage);
    }

    private NotificationEvent create(EmailNotificationCommand command, String eventType, String errorMessage) {
        return new NotificationEvent(
                UUID.randomUUID().toString(),
                SCHEMA_VERSION,
                eventType,
                SOURCE,
                command.commandId(),
                command.correlationId(),
                new NotificationEvent.Actor(command.requestedBy()),
                List.copyOf(command.to()),
                OffsetDateTime.now(),
                errorMessage,
                command.metadata()
        );
    }
}
