package io.github.flaviodotcom.notification.service;

import io.github.flaviodotcom.notification.domain.model.NotificationOutboxItem;
import io.github.flaviodotcom.notification.dto.EmailNotificationCommand;

import java.time.Duration;
import java.util.List;

public interface NotificationOutboxService {

    boolean enqueue(EmailNotificationCommand command);

    List<NotificationOutboxItem> claimPending(int batchSize);

    void markSent(Long outboxId, String commandId);

    void markRetryableFailure(Long outboxId, String commandId, String errorMessage, Duration retryDelay);

    void markFailed(Long outboxId, String commandId, String errorMessage);
}
