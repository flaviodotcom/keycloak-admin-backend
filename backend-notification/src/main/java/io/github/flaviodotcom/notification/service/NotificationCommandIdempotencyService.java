package io.github.flaviodotcom.notification.service;

public interface NotificationCommandIdempotencyService {

    boolean enqueue(String commandId);

    void markProcessing(String commandId);

    void markQueued(String commandId, String errorMessage);

    void markSent(String commandId);

    void markFailed(String commandId, String errorMessage);
}
