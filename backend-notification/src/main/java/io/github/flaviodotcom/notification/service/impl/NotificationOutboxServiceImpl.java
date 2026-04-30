package io.github.flaviodotcom.notification.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.flaviodotcom.notification.domain.model.NotificationOutboxItem;
import io.github.flaviodotcom.notification.domain.model.NotificationOutboxStatus;
import io.github.flaviodotcom.notification.dto.EmailNotificationCommand;
import io.github.flaviodotcom.notification.entities.NotificationEmailOutbox;
import io.github.flaviodotcom.notification.repository.NotificationEmailOutboxRepository;
import io.github.flaviodotcom.notification.service.NotificationCommandIdempotencyService;
import io.github.flaviodotcom.notification.service.NotificationOutboxService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@AllArgsConstructor
@ApplicationScoped
public class NotificationOutboxServiceImpl implements NotificationOutboxService {

    private final ObjectMapper objectMapper;
    private final NotificationEmailOutboxRepository outboxRepository;
    private final NotificationCommandIdempotencyService idempotencyService;

    @Override
    @Transactional
    public boolean enqueue(EmailNotificationCommand command) {
        if (!this.idempotencyService.enqueue(command.commandId())) {
            return false;
        }

        var now = OffsetDateTime.now();
        var existingOutbox = this.outboxRepository.findByCommandId(command.commandId());
        if (existingOutbox.isPresent()) {
            var outbox = existingOutbox.get();
            outbox.payloadJson = this.toJson(command);
            outbox.status = NotificationOutboxStatus.PENDING.name();
            outbox.attempts = 0;
            outbox.updatedAt = now;
            outbox.processedAt = null;
            outbox.nextAttemptAt = null;
            outbox.errorMessage = null;
            return true;
        }

        var outbox = new NotificationEmailOutbox();
        outbox.commandId = command.commandId();
        outbox.payloadJson = this.toJson(command);
        outbox.status = NotificationOutboxStatus.PENDING.name();
        outbox.attempts = 0;
        outbox.createdAt = now;
        outbox.updatedAt = now;
        outbox.nextAttemptAt = null;
        this.outboxRepository.persist(outbox);
        return true;
    }

    @Override
    @Transactional
    public List<NotificationOutboxItem> claimPending(int batchSize) {
        var now = OffsetDateTime.now();
        var outboxes = this.outboxRepository.findPendingForUpdate(batchSize, now);
        outboxes.forEach(outbox -> {
            outbox.status = NotificationOutboxStatus.PROCESSING.name();
            outbox.attempts++;
            outbox.updatedAt = now;
            outbox.nextAttemptAt = null;
            outbox.errorMessage = null;
            this.idempotencyService.markProcessing(outbox.commandId);
        });

        return outboxes.stream()
                .map(outbox -> new NotificationOutboxItem(outbox.id, outbox.attempts, this.fromJson(outbox.payloadJson)))
                .toList();
    }

    @Override
    @Transactional
    public void markSent(Long outboxId, String commandId) {
        var outbox = this.findById(outboxId);
        outbox.status = NotificationOutboxStatus.SENT.name();
        outbox.updatedAt = OffsetDateTime.now();
        outbox.processedAt = outbox.updatedAt;
        outbox.nextAttemptAt = null;
        outbox.errorMessage = null;
        this.idempotencyService.markSent(commandId);
    }

    @Override
    @Transactional
    public void markRetryableFailure(Long outboxId, String commandId, String errorMessage, Duration retryDelay) {
        var outbox = this.findById(outboxId);
        outbox.status = NotificationOutboxStatus.PENDING.name();
        outbox.updatedAt = OffsetDateTime.now();
        outbox.processedAt = null;
        outbox.nextAttemptAt = outbox.updatedAt.plus(retryDelay);
        outbox.errorMessage = errorMessage;
        this.idempotencyService.markQueued(commandId, errorMessage);
    }

    @Override
    @Transactional
    public void markFailed(Long outboxId, String commandId, String errorMessage) {
        var outbox = this.findById(outboxId);
        outbox.status = NotificationOutboxStatus.FAILED.name();
        outbox.updatedAt = OffsetDateTime.now();
        outbox.processedAt = outbox.updatedAt;
        outbox.nextAttemptAt = null;
        outbox.errorMessage = errorMessage;
        this.idempotencyService.markFailed(commandId, errorMessage);
    }

    private NotificationEmailOutbox findById(Long outboxId) {
        return this.outboxRepository.findByIdOptional(outboxId)
                .orElseThrow(() -> new IllegalStateException("Notification outbox entry was not found."));
    }

    private String toJson(EmailNotificationCommand command) {
        try {
            return this.objectMapper.writeValueAsString(command);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Notification command must be serializable.", exception);
        }
    }

    private EmailNotificationCommand fromJson(String payloadJson) {
        try {
            return this.objectMapper.readValue(payloadJson, EmailNotificationCommand.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Notification outbox payload must be valid JSON.", exception);
        }
    }
}
