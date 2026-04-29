package io.github.flaviodotcom.service.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@ApplicationScoped
public class IdentityEventPublisher {

    private static final Logger LOG = Logger.getLogger(IdentityEventPublisher.class);
    private static final String SOURCE = "backend-keycloak";

    private final ObjectMapper objectMapper;
    private final RequestActorResolver actorResolver;
    private final RequestCorrelationIdResolver correlationIdResolver;

    @ConfigProperty(name = "identity.events.enabled")
    private final boolean enabled;

    @Channel("identity-events")
    private final Emitter<String> identityEvents;

    public void publish(String eventType, String subjectType, String subjectId, Map<String, Object> data) {
        if (!this.enabled) {
            return;
        }

        var event = new IdentityEvent(
                UUID.randomUUID().toString(),
                1,
                eventType,
                SOURCE,
                this.correlationIdResolver.resolve(),
                new IdentityEvent.Actor(this.actorResolver.resolve()),
                new IdentityEvent.Subject(subjectType, subjectId),
                OffsetDateTime.now(),
                data
        );
        this.identityEvents.send(this.toJson(event)).toCompletableFuture().join();
        LOG.infof(
                "Identity event published eventId=%s eventType=%s correlationId=%s actor=%s subjectType=%s subjectId=%s",
                event.eventId(),
                event.eventType(),
                event.correlationId(),
                event.actor().id(),
                event.subject().type(),
                event.subject().id()
        );
    }

    private String toJson(IdentityEvent event) {
        try {
            return this.objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Identity event must be serializable.", exception);
        }
    }
}
