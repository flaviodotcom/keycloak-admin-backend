package io.github.flaviodotcom.infrastructure.interception.identityevent;

import io.github.flaviodotcom.infrastructure.interception.contracts.ActionPayload;
import io.github.flaviodotcom.service.events.IdentityEventPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@ApplicationScoped
public class IdentityEventActionHandler {

    private final IdentityEventPublisher publisher;

    public void handle(PublishIdentityEvent annotation,
                       ActionPayload payload) {

        this.publisher.publish(
                annotation.eventType(),
                annotation.subjectType(),
                payload.actionSubjectId(),
                payload.actionMetadata()
        );
    }
}
