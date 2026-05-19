package io.github.flaviodotcom.infrastructure.messaging.noop;

import io.github.flaviodotcom.infrastructure.messaging.qualifiers.NoOpPublisher;
import io.github.flaviodotcom.service.notifications.EmailNotificationCommand;
import io.github.flaviodotcom.service.notifications.NotificationCommandPublisher;
import jakarta.enterprise.context.ApplicationScoped;

@NoOpPublisher
@ApplicationScoped
public class NoOpNotificationCommandPublisher implements NotificationCommandPublisher {
    @Override
    public void publish(EmailNotificationCommand command) {
        // no-op
    }
}
