package io.github.flaviodotcom.notification.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.flaviodotcom.notification.dto.EmailNotificationCommand;
import io.github.flaviodotcom.notification.service.EmailNotificationService;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@Slf4j
@AllArgsConstructor
@ApplicationScoped
public class NotificationCommandConsumer {

    private final ObjectMapper objectMapper;
    private final EmailNotificationService emailNotificationService;

    @Incoming("notification-commands")
    @Blocking
    public void consume(String payloadJson) {
        var command = this.readCommand(payloadJson);
        log.info(
                "Notification command consumed commandId={} correlationId={} actor={} recipients={}",
                command.commandId(),
                command.correlationId(),
                command.requestedBy(),
                command.to()
        );
        this.emailNotificationService.send(command);
    }

    private EmailNotificationCommand readCommand(String payloadJson) {
        try {
            return this.objectMapper.readValue(payloadJson, EmailNotificationCommand.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Notification command payload must be valid JSON.", exception);
        }
    }
}
