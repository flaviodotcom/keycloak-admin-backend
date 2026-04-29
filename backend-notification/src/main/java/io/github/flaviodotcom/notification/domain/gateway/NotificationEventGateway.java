package io.github.flaviodotcom.notification.domain.gateway;

import io.github.flaviodotcom.notification.dto.NotificationEvent;

public interface NotificationEventGateway {

    void publish(NotificationEvent event);
}
