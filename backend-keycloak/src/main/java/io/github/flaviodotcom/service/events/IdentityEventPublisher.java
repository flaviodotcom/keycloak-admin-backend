package io.github.flaviodotcom.service.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class IdentityEventPublisher {

    private static final String SOURCE = "backend-keycloak";

    private final ObjectMapper objectMapper;
    private final RequestActorResolver actorResolver;

    @ConfigProperty(name = "identity.events.enabled")
    boolean enabled;

    @Channel("identity-events")
    Emitter<String> identityEvents;

    @Inject
    public IdentityEventPublisher(ObjectMapper objectMapper, RequestActorResolver actorResolver) {
        this.objectMapper = objectMapper;
        this.actorResolver = actorResolver;
    }

    public void publish(String eventType, String subjectType, String subjectId, Map<String, Object> data) {
        if (!this.enabled) {
            return;
        }

        var event = new IdentityEvent(
                UUID.randomUUID().toString(),
                1,
                eventType,
                SOURCE,
                new IdentityEvent.Actor(this.actorResolver.resolve()),
                new IdentityEvent.Subject(subjectType, subjectId),
                OffsetDateTime.now(),
                data
        );
        this.identityEvents.send(this.toJson(event)).toCompletableFuture().join();
    }

    private String toJson(IdentityEvent event) {
        try {
            return this.objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Identity event must be serializable.", exception);
        }
    }
}
