package io.github.flaviodotcom.health;

import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import jakarta.ws.rs.ProcessingException;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmRepresentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KeycloakReadinessHealthCheckTest {

    @Test
    void givenReachableKeycloak_WhenCall_ThenReturnUp() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var realm = mock(RealmResource.class);
        when(keycloak.realm()).thenReturn(realm);
        when(realm.toRepresentation()).thenReturn(new RealmRepresentation());

        var response = new KeycloakReadinessHealthCheck(keycloak).call();

        assertEquals("keycloak", response.getName());
        assertEquals(HealthCheckResponse.Status.UP, response.getStatus());
        verify(realm).toRepresentation();
    }

    @Test
    void givenUnavailableKeycloak_WhenCall_ThenPropagateError() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var realm = mock(RealmResource.class);
        var exception = new ProcessingException("Keycloak unavailable");
        when(keycloak.realm()).thenReturn(realm);
        when(realm.toRepresentation()).thenThrow(exception);

        var healthCheck = new KeycloakReadinessHealthCheck(keycloak);

        assertThrows(ProcessingException.class, healthCheck::call);
    }
}
