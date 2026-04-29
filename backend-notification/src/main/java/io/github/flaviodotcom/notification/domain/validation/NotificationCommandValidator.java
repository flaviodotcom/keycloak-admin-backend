package io.github.flaviodotcom.notification.domain.validation;

import io.github.flaviodotcom.notification.dto.EmailNotificationCommand;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotificationCommandValidator {

    private static final int SUPPORTED_SCHEMA_VERSION = 1;

    public void validate(EmailNotificationCommand command) {
        if (command.schemaVersion() == null) {
            throw new IllegalArgumentException("Notification command field 'schemaVersion' is required.");
        }
        if (command.schemaVersion() != SUPPORTED_SCHEMA_VERSION) {
            throw new IllegalArgumentException("Notification command schemaVersion must be 1.");
        }
    }
}
