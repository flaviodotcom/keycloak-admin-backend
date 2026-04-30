package io.github.flaviodotcom.notification.service.impl;

import io.github.flaviodotcom.notification.domain.validation.NotificationCommandValidator;
import io.github.flaviodotcom.notification.dto.EmailNotificationCommand;
import io.github.flaviodotcom.notification.service.EmailNotificationService;
import io.github.flaviodotcom.notification.service.NotificationOutboxService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@ApplicationScoped
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final NotificationCommandValidator commandValidator;
    private final NotificationOutboxService outboxService;

    @Override
    public void send(EmailNotificationCommand command) {
        this.commandValidator.validate(command);

        if (!this.outboxService.enqueue(command)) {
            log.info(
                    "Notification command already enqueued or processed commandId={} correlationId={} actor={} recipients={}",
                    command.commandId(),
                    command.correlationId(),
                    command.requestedBy(),
                    command.to()
            );
            return;
        }

        log.info(
                "Notification command enqueued commandId={} correlationId={} actor={} recipients={}",
                command.commandId(),
                command.correlationId(),
                command.requestedBy(),
                command.to()
        );
    }
}
