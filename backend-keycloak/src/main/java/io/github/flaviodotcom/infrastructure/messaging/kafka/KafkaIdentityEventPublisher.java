package io.github.flaviodotcom.infrastructure.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.flaviodotcom.infrastructure.messaging.qualifiers.KafkaPublisher;
import io.github.flaviodotcom.service.events.IdentityEvent;
import io.github.flaviodotcom.service.events.IdentityEventPublisher;
import io.github.flaviodotcom.service.events.RequestActorResolver;
import io.github.flaviodotcom.service.events.RequestCorrelationIdResolver;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@KafkaPublisher
@AllArgsConstructor
@ApplicationScoped
public class KafkaIdentityEventPublisher implements IdentityEventPublisher {

    private static final String SOURCE = "backend-keycloak";

    private final ObjectMapper objectMapper;
    private final RequestActorResolver actorResolver;
    private final RequestCorrelationIdResolver correlationIdResolver;

    @Channel("identity-events")
    private final Emitter<String> identityEvents;

    @Override
    public void publish(String eventType, String subjectType, String subjectId, Map<String, Object> data) {
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
        log.info(
                "Identity event published eventId={} eventType={} correlationId={} actor={} subjectType={} subjectId={}",
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
