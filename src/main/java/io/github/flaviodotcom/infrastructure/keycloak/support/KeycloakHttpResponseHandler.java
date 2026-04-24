package io.github.flaviodotcom.infrastructure.keycloak.support;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public final class KeycloakHttpResponseHandler {

    private KeycloakHttpResponseHandler() {
    }

    public static void ensureCreated(Response response) {
        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            return;
        }

        throw toWebApplicationException(response);
    }

    public static WebApplicationException toWebApplicationException(Response response) {
        var detail = response.hasEntity()
                ? response.readEntity(String.class)
                : response.getStatusInfo().getReasonPhrase();
        return new WebApplicationException(detail, response.getStatus());
    }
}
