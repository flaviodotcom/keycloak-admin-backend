package io.github.flaviodotcom.notification.domain.validation;

import io.github.flaviodotcom.notification.dto.EmailNotificationCommand;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@ApplicationScoped
public class NotificationCommandValidator {

    private static final int SUPPORTED_SCHEMA_VERSION = 1;

    private final Validator validator;

    public void validate(EmailNotificationCommand command) {
        var violations = this.validator.validate(command);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        if (command.schemaVersion() == null) {
            throw new IllegalArgumentException("Notification command field 'schemaVersion' is required.");
        }
        if (command.schemaVersion() != SUPPORTED_SCHEMA_VERSION) {
            throw new IllegalArgumentException("Notification command schemaVersion must be 1.");
        }
        if (isBlank(command.textBody()) && isBlank(command.htmlBody())) {
            throw new IllegalArgumentException("Either textBody or htmlBody must be provided.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
