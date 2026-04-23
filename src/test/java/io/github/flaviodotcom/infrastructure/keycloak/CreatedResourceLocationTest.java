package io.github.flaviodotcom.infrastructure.keycloak;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreatedResourceLocationTest {

    @Test
    void givenCreatedResponse_WhenExtractId_ThenReturnLastPathSegment() {
        var response = Response.created(URI.create("http://localhost:8080/admin/realms/test/users/123")).build();

        var resourceId = CreatedResourceLocation.extractId(response);

        assertEquals("123", resourceId);
    }

    @Test
    void givenResponseWithoutLocation_WhenExtractId_ThenThrowException() {
        var response = Response.status(Response.Status.CREATED).build();

        var exception = assertThrows(IllegalStateException.class, () -> CreatedResourceLocation.extractId(response));

        assertEquals("Keycloak did not return a location header for the created resource.", exception.getMessage());
    }
}
