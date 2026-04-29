package io.github.flaviodotcom.notification.service.impl;

import io.github.flaviodotcom.notification.domain.factory.NotificationEventFactory;
import io.github.flaviodotcom.notification.domain.gateway.EmailGateway;
import io.github.flaviodotcom.notification.domain.gateway.NotificationEventGateway;
import io.github.flaviodotcom.notification.domain.validation.NotificationCommandValidator;
import io.github.flaviodotcom.notification.dto.EmailNotificationCommand;
import io.github.flaviodotcom.notification.service.EmailNotificationService;
import io.github.flaviodotcom.notification.service.NotificationCommandIdempotencyService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@ApplicationScoped
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final EmailGateway emailGateway;
    private final NotificationEventGateway eventGateway;
    private final NotificationCommandValidator commandValidator;
    private final NotificationEventFactory eventFactory;
    private final NotificationCommandIdempotencyService idempotencyService;

    @Override
    public void send(EmailNotificationCommand command) {
        this.commandValidator.validate(command);

        if (!this.idempotencyService.startProcessing(command.commandId())) {
            log.info(
                    "Notification command already processed commandId={} correlationId={} actor={} recipients={}",
                    command.commandId(),
                    command.correlationId(),
                    command.requestedBy(),
                    command.to()
            );
            return;
        }

        try {
            this.emailGateway.send(command);
            this.eventGateway.publish(this.eventFactory.sent(command));
            this.idempotencyService.markSent(command.commandId());
            log.info(
                    "Notification command sent commandId={} correlationId={} actor={} recipients={}",
                    command.commandId(),
                    command.correlationId(),
                    command.requestedBy(),
                    command.to()
            );
        } catch (RuntimeException exception) {
            this.eventGateway.publish(this.eventFactory.failed(command, exception.getMessage()));
            this.idempotencyService.markFailed(command.commandId(), exception.getMessage());
            log.error(
                    "Notification command failed commandId={} correlationId={} actor={} recipients={} error={}",
                    command.commandId(),
                    command.correlationId(),
                    command.requestedBy(),
                    command.to(),
                    exception.getMessage(),
                    exception
            );
            throw exception;
        }
    }
}
