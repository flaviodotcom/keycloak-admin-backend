package io.github.flaviodotcom.infrastructure.interception.identityevent;

import io.github.flaviodotcom.service.events.RequestActorResolver;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.AllArgsConstructor;

@Provider
@RequireActorHeader
@Priority(Priorities.AUTHENTICATION)
@AllArgsConstructor
public class RequireActorHeaderFilter implements ContainerRequestFilter {

    private final SecurityIdentity securityIdentity;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (!this.securityIdentity.isAnonymous()) {
            return;
        }

        var actorId = requestContext.getHeaderString(RequestActorResolver.ACTOR_HEADER);

        if (actorId == null || actorId.isBlank()) {
            throw new BadRequestException(
                    "%s header is required when identity events are enabled."
                            .formatted(RequestActorResolver.ACTOR_HEADER)
            );
        }
    }
}
