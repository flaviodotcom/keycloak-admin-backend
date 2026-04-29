package io.github.flaviodotcom.notification.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.flaviodotcom.notification.dto.NotificationEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class NotificationEventPublisher {

    private final ObjectMapper objectMapper;

    @Channel("notification-events")
    Emitter<String> notificationEvents;

    @Inject
    public NotificationEventPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void publish(NotificationEvent event) {
        this.notificationEvents.send(this.toJson(event)).toCompletableFuture().join();
    }

    private String toJson(NotificationEvent event) {
        try {
            return this.objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Notification event must be serializable.", exception);
        }
    }
}
