package io.github.flaviodotcom.infrastructure.interception.identityevent;

import io.github.flaviodotcom.exceptions.LocalizedBadRequestException;
import io.github.flaviodotcom.i18n.Messages;
import io.github.flaviodotcom.service.events.RequestActorResolver;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
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

        var actorHeader = RequestActorResolver.ACTOR_HEADER;
        var actorId = requestContext.getHeaderString(actorHeader);

        if (actorId == null || actorId.isBlank()) {
            var messageKey = "error.request-actor-header.required";
            throw new LocalizedBadRequestException(
                    messageKey,
                    Messages.getDefault(messageKey, actorHeader),
                    actorHeader
            );
        }
    }
}
