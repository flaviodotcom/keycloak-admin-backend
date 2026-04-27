package io.github.flaviodotcom.infrastructure.keycloak.support;

import io.github.flaviodotcom.exceptions.LocalizedMessage;
import io.github.flaviodotcom.exceptions.LocalizedWebApplicationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public final class KeycloakHttpResponseHandler {

    private KeycloakHttpResponseHandler() {
    }

    public static void ensureCreated(Response response) {
        ensureCreated(response, KeycloakErrorContext.GENERAL);
    }

    public static void ensureCreated(Response response, KeycloakErrorContext context) {
        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            return;
        }

        throw toWebApplicationException(response, context);
    }

    public static WebApplicationException toWebApplicationException(Response response) {
        return toWebApplicationException(response, KeycloakErrorContext.GENERAL);
    }

    public static WebApplicationException toWebApplicationException(Response response, KeycloakErrorContext context) {
        var detail = response.hasEntity()
                ? response.readEntity(String.class)
                : response.getStatusInfo().getReasonPhrase();
        var translatedError = KeycloakErrorTranslator.translate(response.getStatus(), detail, context);
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

    public static WebApplicationException toWebApplicationException(WebApplicationException exception, KeycloakErrorContext context) {
        if (exception instanceof LocalizedMessage) {
            return exception;
        }

        return toWebApplicationException(exception.getResponse(), context);
    }
}
