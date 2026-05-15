package io.github.flaviodotcom.infrastructure.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.flaviodotcom.service.notifications.EmailNotificationCommand;
import io.github.flaviodotcom.service.notifications.NotificationCommandPublisher;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@Slf4j
@IfBuildProfile("kafka")
@AllArgsConstructor
@ApplicationScoped
public class KafkaNotificationCommandPublisher implements NotificationCommandPublisher {

    private final ObjectMapper objectMapper;

    @Channel("notification-commands")
    private final Emitter<String> notificationCommands;

    @Override
    public void publish(EmailNotificationCommand command) {
        this.notificationCommands.send(this.toJson(command)).toCompletableFuture().join();
        log.info(
                "Notification command published commandId={} correlationId={} actor={} recipients={}",
                command.commandId(),
                command.correlationId(),
                command.requestedBy(),
                command.to()
        );
    }

    private String toJson(EmailNotificationCommand command) {
        try {
            return this.objectMapper.writeValueAsString(command);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Notification command must be serializable.", exception);
        }
    }
}
