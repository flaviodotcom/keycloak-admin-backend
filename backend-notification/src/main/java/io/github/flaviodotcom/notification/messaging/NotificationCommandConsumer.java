package io.github.flaviodotcom.notification.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.flaviodotcom.notification.dto.EmailNotificationCommand;
import io.github.flaviodotcom.notification.service.EmailNotificationService;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class NotificationCommandConsumer {

    private final ObjectMapper objectMapper;
    private final EmailNotificationService emailNotificationService;

    public NotificationCommandConsumer(ObjectMapper objectMapper, EmailNotificationService emailNotificationService) {
        this.objectMapper = objectMapper;
        this.emailNotificationService = emailNotificationService;
    }

    @Incoming("notification-commands")
    @Blocking
    public void consume(String payloadJson) {
        this.emailNotificationService.send(this.readCommand(payloadJson));
    }

    private EmailNotificationCommand readCommand(String payloadJson) {
        try {
            return this.objectMapper.readValue(payloadJson, EmailNotificationCommand.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Notification command payload must be valid JSON.", exception);
        }
    }
}
