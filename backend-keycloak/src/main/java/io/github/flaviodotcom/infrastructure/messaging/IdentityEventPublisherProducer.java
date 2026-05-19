package io.github.flaviodotcom.infrastructure.messaging;

import io.github.flaviodotcom.config.properties.IdentityEventProperties;
import io.github.flaviodotcom.infrastructure.messaging.qualifiers.KafkaPublisher;
import io.github.flaviodotcom.infrastructure.messaging.qualifiers.NoOpPublisher;
import io.github.flaviodotcom.service.events.IdentityEventPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ApplicationScoped
public class IdentityEventPublisherProducer {

    @Inject
    IdentityEventProperties properties;

    @Inject
    @KafkaPublisher
    IdentityEventPublisher kafkaPublisher;

    @Inject
    @NoOpPublisher
    IdentityEventPublisher noopPublisher;

    @Produces
    @ApplicationScoped
    public IdentityEventPublisher produce() {
        return properties.enabled() ? kafkaPublisher : noopPublisher;
    }
}
