package io.github.flaviodotcom.infrastructure.keycloak.support;

import io.github.flaviodotcom.exceptions.LocalizedWebApplicationException;
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
        var translatedError = KeycloakErrorTranslator.translate(response.getStatus(), detail);
        if (translatedError.localized()) {
            return new LocalizedWebApplicationException(
                    response.getStatus(),
                    translatedError.messageKey(),
                    translatedError.detail(),
                    translatedError.messageArgs()
            );
        }

        return new WebApplicationException(translatedError.detail(), response.getStatus());
    }
}
