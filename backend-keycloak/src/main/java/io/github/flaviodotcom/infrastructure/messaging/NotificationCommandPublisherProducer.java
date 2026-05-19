package io.github.flaviodotcom.infrastructure.messaging;

import io.github.flaviodotcom.config.properties.NotificationProperties;
import io.github.flaviodotcom.infrastructure.messaging.qualifiers.KafkaPublisher;
import io.github.flaviodotcom.infrastructure.messaging.qualifiers.NoOpPublisher;
import io.github.flaviodotcom.service.notifications.NotificationCommandPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ApplicationScoped
public class NotificationCommandPublisherProducer {

    @Inject
    NotificationProperties properties;

    @Inject
    @KafkaPublisher
    NotificationCommandPublisher kafkaPublisher;

    @Inject
    @NoOpPublisher
    NotificationCommandPublisher noopPublisher;

    @Produces
    @ApplicationScoped
    public NotificationCommandPublisher produce() {
        return properties.commands().enabled() ? kafkaPublisher : noopPublisher;
    }
}