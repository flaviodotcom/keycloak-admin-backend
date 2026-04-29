package io.github.flaviodotcom.notification.service;

public interface NotificationCommandIdempotencyService {

    boolean startProcessing(String commandId);

    void markSent(String commandId);

    void markFailed(String commandId, String errorMessage);
}
