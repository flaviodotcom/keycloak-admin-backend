package io.github.flaviodotcom.service.events;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
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

        return this.headers.getHeaderString(ACTOR_HEADER);
    }
}
