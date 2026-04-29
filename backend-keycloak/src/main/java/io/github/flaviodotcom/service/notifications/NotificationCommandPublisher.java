package io.github.flaviodotcom.service.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class NotificationCommandPublisher {

    private final ObjectMapper objectMapper;

    @Channel("notification-commands")
    Emitter<String> notificationCommands;

    @Inject
    public NotificationCommandPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void publish(EmailNotificationCommand command) {
        this.notificationCommands.send(this.toJson(command)).toCompletableFuture().join();
    }

    private String toJson(EmailNotificationCommand command) {
        try {
            return this.objectMapper.writeValueAsString(command);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Notification command must be serializable.", exception);
        }
    }
}
