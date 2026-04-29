package io.github.flaviodotcom.notification.resources;

import io.github.flaviodotcom.notification.dto.EmailNotificationCommand;
import io.github.flaviodotcom.notification.dto.NotificationResponse;
import io.github.flaviodotcom.notification.service.EmailNotificationService;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Path("/v1/notifications/emails")
public class EmailNotificationResource {

    private final EmailNotificationService emailNotificationService;

    @POST
    public NotificationResponse send(@Valid EmailNotificationCommand command) {
        this.emailNotificationService.send(command);
        return new NotificationResponse(command.commandId());
    }
}
