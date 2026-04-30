package io.github.flaviodotcom.notification.domain.gateway;

import io.github.flaviodotcom.notification.dto.EmailNotificationCommand;

public interface EmailGateway {

    void send(EmailNotificationCommand command);
}
