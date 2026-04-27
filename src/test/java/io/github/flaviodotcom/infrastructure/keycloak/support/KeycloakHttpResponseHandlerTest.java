package io.github.flaviodotcom.infrastructure.keycloak.support;

import io.github.flaviodotcom.exceptions.LocalizedWebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakHttpResponseHandlerTest {

    @Test
    void givenKeycloakConflictResponse_WhenToWebApplicationException_ThenTranslateDetail() {
        var response = Response.status(409)
                .entity("{\"errorMessage\":\"User exists with same username or email\"}")
                .build();

        var exception = KeycloakHttpResponseHandler.toWebApplicationException(response);

        assertEquals(409, exception.getResponse().getStatus());
        assertTrue(exception.getMessage().contains("A user already exists with the provided username or email."));
    }

    @Test
    void givenGenericConflictDuringUserCreation_WhenToWebApplicationException_ThenTranslateDetail() {
        var response = Response.status(409).build();

        var exception = KeycloakHttpResponseHandler.toWebApplicationException(response, KeycloakErrorContext.USER_CREATION);

        assertEquals(409, exception.getResponse().getStatus());
        assertTrue(exception.getMessage().contains("A user already exists with the provided username or email."));
    }

    @Test
    void givenLocalizedException_WhenToWebApplicationException_ThenPreserveOriginalException() {
        var exception = new LocalizedWebApplicationException(
                409,
                "keycloak.error.user-conflict.username-or-email",
                "A user already exists with the provided username or email."
        );

        var translated = KeycloakHttpResponseHandler.toWebApplicationException(exception, KeycloakErrorContext.USER_CREATION);

        assertSame(exception, translated);
    }

    @Test
    void givenUpdatePasswordEmailFailure_WhenToWebApplicationException_ThenTranslateDetail() {
        var response = Response.status(500).build();

        var exception = KeycloakHttpResponseHandler.toWebApplicationException(response, KeycloakErrorContext.UPDATE_PASSWORD_EMAIL);

        assertEquals(500, exception.getResponse().getStatus());
        assertTrue(exception.getMessage().contains("Could not send the update password email. Check the SMTP configuration."));
    }
}
