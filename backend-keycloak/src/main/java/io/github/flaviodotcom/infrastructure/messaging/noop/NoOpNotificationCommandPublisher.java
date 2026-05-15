package io.github.flaviodotcom.infrastructure.messaging.noop;

import io.github.flaviodotcom.service.notifications.EmailNotificationCommand;
import io.github.flaviodotcom.service.notifications.NotificationCommandPublisher;
import io.quarkus.arc.profile.UnlessBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;

@UnlessBuildProfile("kafka")
@ApplicationScoped
public class NoOpNotificationCommandPublisher implements NotificationCommandPublisher {
    @Override
    public void publish(EmailNotificationCommand command) {
        // no-op
    }
}
