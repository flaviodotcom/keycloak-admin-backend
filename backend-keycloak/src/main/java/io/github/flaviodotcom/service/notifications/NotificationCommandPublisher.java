package io.github.flaviodotcom.service.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@AllArgsConstructor
@ApplicationScoped
public class NotificationCommandPublisher {

    private static final Logger LOG = Logger.getLogger(NotificationCommandPublisher.class);

    private final ObjectMapper objectMapper;

    @Channel("notification-commands")
    private final Emitter<String> notificationCommands;

    public void publish(EmailNotificationCommand command) {
        this.notificationCommands.send(this.toJson(command)).toCompletableFuture().join();
        LOG.infof(
                "Notification command published commandId=%s correlationId=%s actor=%s recipients=%s",
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
