package io.github.flaviodotcom.notification.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.flaviodotcom.notification.domain.gateway.NotificationEventGateway;
import io.github.flaviodotcom.notification.dto.NotificationEvent;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@Slf4j
@AllArgsConstructor
@ApplicationScoped
public class KafkaNotificationEventPublisher implements NotificationEventGateway {

    private final ObjectMapper objectMapper;
    @Channel("notification-events")
    private final Emitter<String> notificationEvents;

    @Override
    public void publish(NotificationEvent event) {
        this.notificationEvents.send(this.toJson(event)).toCompletableFuture().join();
        log.info(
                "Notification event published eventId={} eventType={} commandId={} correlationId={} actor={} recipients={}",
                event.eventId(),
                event.eventType(),
                event.commandId(),
                event.correlationId(),
                event.actor().id(),
                event.recipients()
        );
    }

    private String toJson(NotificationEvent event) {
        try {
            return this.objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Notification event must be serializable.", exception);
        }
    }
}
