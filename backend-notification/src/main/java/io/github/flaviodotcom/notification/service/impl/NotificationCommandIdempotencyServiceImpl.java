package io.github.flaviodotcom.notification.service.impl;

import io.github.flaviodotcom.notification.domain.model.NotificationCommandStatus;
import io.github.flaviodotcom.notification.entities.ProcessedNotificationCommand;
import io.github.flaviodotcom.notification.repository.ProcessedNotificationCommandRepository;
import io.github.flaviodotcom.notification.service.NotificationCommandIdempotencyService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;

@AllArgsConstructor
@ApplicationScoped
public class NotificationCommandIdempotencyServiceImpl implements NotificationCommandIdempotencyService {

    private final ProcessedNotificationCommandRepository repository;

    @Override
    @Transactional
    public boolean startProcessing(String commandId) {
        var now = OffsetDateTime.now();
        var existingCommand = this.repository.findByCommandId(commandId);

        if (existingCommand.isPresent()) {
            var command = existingCommand.get();
            if (NotificationCommandStatus.SENT.name().equals(command.status)
                    || NotificationCommandStatus.PROCESSING.name().equals(command.status)) {
                return false;
            }

            command.status = NotificationCommandStatus.PROCESSING.name();
            command.updatedAt = now;
            command.errorMessage = null;
            return true;
        }

        var command = new ProcessedNotificationCommand();
        command.commandId = commandId;
        command.status = NotificationCommandStatus.PROCESSING.name();
        command.createdAt = now;
        command.updatedAt = now;
        this.repository.persist(command);
        return true;
    }

    @Override
    @Transactional
    public void markSent(String commandId) {
        var command = this.repository.findByCommandId(commandId)
                .orElseThrow(() -> new IllegalStateException("Notification command was not registered before marking as sent."));
        command.status = NotificationCommandStatus.SENT.name();
        command.updatedAt = OffsetDateTime.now();
        command.errorMessage = null;
    }

    @Override
    @Transactional
    public void markFailed(String commandId, String errorMessage) {
        var command = this.repository.findByCommandId(commandId)
                .orElseThrow(() -> new IllegalStateException("Notification command was not registered before marking as failed."));
        command.status = NotificationCommandStatus.FAILED.name();
        command.updatedAt = OffsetDateTime.now();
        command.errorMessage = errorMessage;
    }
}
