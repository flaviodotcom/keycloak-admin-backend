package io.github.flaviodotcom.notification.service.impl;

import io.github.flaviodotcom.notification.config.properties.NotificationOutboxProperties;
import io.github.flaviodotcom.notification.domain.factory.NotificationEventFactory;
import io.github.flaviodotcom.notification.domain.gateway.EmailGateway;
import io.github.flaviodotcom.notification.domain.gateway.NotificationEventGateway;
import io.github.flaviodotcom.notification.domain.model.NotificationOutboxItem;
import io.github.flaviodotcom.notification.service.NotificationOutboxProcessor;
import io.github.flaviodotcom.notification.service.NotificationOutboxService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@ApplicationScoped
public class NotificationOutboxProcessorImpl implements NotificationOutboxProcessor {

    private final EmailGateway emailGateway;
    private final NotificationEventGateway eventGateway;
    private final NotificationEventFactory eventFactory;
    private final NotificationOutboxService outboxService;
    private final NotificationOutboxProperties properties;

    @Override
    public int processPending() {
        var processed = 0;
        for (var index = 0; index < this.properties.batchSize(); index++) {
            var items = this.outboxService.claimPending(1);
            if (items.isEmpty()) {
                return processed;
            }
            this.process(items.getFirst());
            processed++;
        }
        return processed;
    }

    private void process(NotificationOutboxItem item) {
        var command = item.command();
        try {
            this.emailGateway.send(command);
            this.outboxService.markSent(item.outboxId(), command.commandId());
            this.eventGateway.publish(this.eventFactory.sent(command));
            log.info(
                    "Notification outbox sent commandId={} correlationId={} actor={} recipients={}",
                    command.commandId(),
                    command.correlationId(),
                    command.requestedBy(),
                    command.to()
            );
        } catch (RuntimeException exception) {
            if (item.attempt() < this.properties.maxAttempts()) {
                this.outboxService.markRetryableFailure(
                        item.outboxId(),
                        command.commandId(),
                        exception.getMessage(),
                        this.properties.retryDelay()
                );
                log.warn(
                        "Notification outbox retry scheduled commandId={} correlationId={} actor={} recipients={} attempt={} maxAttempts={} error={}",
                        command.commandId(),
                        command.correlationId(),
                        command.requestedBy(),
                        command.to(),
                        item.attempt(),
                        this.properties.maxAttempts(),
                        exception.getMessage(),
                        exception
                );
                throw exception;
            }

            this.markFinalFailure(item, exception);
            throw exception;
        }
    }

    private void markFinalFailure(NotificationOutboxItem item, RuntimeException exception) {
        var command = item.command();
        this.outboxService.markFailed(item.outboxId(), command.commandId(), exception.getMessage());
        this.eventGateway.publish(this.eventFactory.failed(command, exception.getMessage()));
        log.error(
                "Notification outbox failed commandId={} correlationId={} actor={} recipients={} attempt={} maxAttempts={} error={}",
                command.commandId(),
                command.correlationId(),
                command.requestedBy(),
                command.to(),
                item.attempt(),
                this.properties.maxAttempts(),
                exception.getMessage(),
                exception
        );
    }
}
