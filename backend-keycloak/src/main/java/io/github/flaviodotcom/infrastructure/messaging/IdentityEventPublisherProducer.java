package io.github.flaviodotcom.infrastructure.messaging;

import io.github.flaviodotcom.config.properties.IdentityEventProperties;
import io.github.flaviodotcom.infrastructure.messaging.qualifiers.KafkaPublisher;
import io.github.flaviodotcom.infrastructure.messaging.qualifiers.NoOpPublisher;
import io.github.flaviodotcom.service.events.IdentityEventPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class IdentityEventPublisherProducer {

    private final IdentityEventProperties properties;
    private final IdentityEventPublisher kafkaPublisher;
    private final IdentityEventPublisher noopPublisher;

    public IdentityEventPublisherProducer(
            IdentityEventProperties properties,
            @KafkaPublisher IdentityEventPublisher kafkaPublisher,
            @NoOpPublisher IdentityEventPublisher noopPublisher
    ) {
        this.properties = properties;
        this.kafkaPublisher = kafkaPublisher;
        this.noopPublisher = noopPublisher;
    }

    @Produces
    @ApplicationScoped
    public IdentityEventPublisher produce() {
        return properties.enabled()
                ? kafkaPublisher
                : noopPublisher;
    }
}
