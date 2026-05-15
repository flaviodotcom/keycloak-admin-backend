package io.github.flaviodotcom.infrastructure.messaging.noop;

import io.github.flaviodotcom.service.events.IdentityEventPublisher;
import io.quarkus.arc.profile.UnlessBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

@UnlessBuildProfile("kafka")
@ApplicationScoped
public class NoOpIdentityEventPublisher implements IdentityEventPublisher {
    @Override
    public void publish(String eventType, String subjectType, String subjectId, Map<String, Object> data) {
        // no-op
    }
}
