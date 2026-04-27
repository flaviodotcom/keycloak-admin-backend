package io.github.flaviodotcom.infrastructure.keycloak.support;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
