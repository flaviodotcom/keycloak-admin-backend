package io.github.flaviodotcom.infrastructure.messaging;

import io.github.flaviodotcom.config.properties.NotificationProperties;
import io.github.flaviodotcom.infrastructure.messaging.qualifiers.KafkaPublisher;
import io.github.flaviodotcom.infrastructure.messaging.qualifiers.NoOpPublisher;
import io.github.flaviodotcom.service.notifications.NotificationCommandPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class NotificationCommandPublisherProducer {

    private final NotificationProperties properties;
    private final NotificationCommandPublisher kafkaPublisher;
    private final NotificationCommandPublisher noopPublisher;

    public NotificationCommandPublisherProducer(
            NotificationProperties properties,
            @KafkaPublisher NotificationCommandPublisher kafkaPublisher,
            @NoOpPublisher NotificationCommandPublisher noopPublisher
    ) {
        this.properties = properties;
        this.kafkaPublisher = kafkaPublisher;
        this.noopPublisher = noopPublisher;
    }

    @Produces
    @ApplicationScoped
    public NotificationCommandPublisher produce() {
        return properties.commands().enabled()
                ? kafkaPublisher
                : noopPublisher;
    }
}
