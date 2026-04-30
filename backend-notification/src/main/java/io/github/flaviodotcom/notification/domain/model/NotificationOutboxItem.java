package io.github.flaviodotcom.notification.domain.model;

import io.github.flaviodotcom.notification.dto.EmailNotificationCommand;

public record NotificationOutboxItem(
        Long outboxId,
        int attempt,
        EmailNotificationCommand command
) {
}
