package io.github.flaviodotcom.service.events;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.AllArgsConstructor;

@RequestScoped
@AllArgsConstructor
public class RequestActorResolver {

    public static final String ACTOR_HEADER = "X-Actor-Id";

    private final HttpHeaders headers;
    private final SecurityIdentity securityIdentity;

    public String resolve() {
        if (!this.securityIdentity.isAnonymous()) {
            return this.securityIdentity.getPrincipal().getName();
        }

        var actorIds = this.headers.getRequestHeader(ACTOR_HEADER);
        if (actorIds == null || actorIds.isEmpty() || actorIds.getFirst().isBlank()) {
            throw new BadRequestException("%s header is required when identity events are enabled.".formatted(ACTOR_HEADER));
        }
        return actorIds.getFirst();
    }
}
