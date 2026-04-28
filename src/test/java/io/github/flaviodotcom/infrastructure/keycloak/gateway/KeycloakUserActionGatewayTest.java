package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import io.github.flaviodotcom.infrastructure.keycloak.resilience.KeycloakResilienceExecutor;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KeycloakUserActionGatewayTest {

    @Test
    void givenUserId_WhenSendUpdatePasswordEmail_ThenExecuteRequiredActionEmail() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var usersResource = mock(UsersResource.class);
        var userResource = mock(UserResource.class);
        var gateway = new KeycloakUserActionGateway(keycloak, new KeycloakResilienceExecutor());

        when(keycloak.users()).thenReturn(usersResource);
        when(usersResource.get("user-1")).thenReturn(userResource);

        gateway.sendUpdatePasswordEmail("user-1");

        verify(userResource).executeActionsEmail(List.of("UPDATE_PASSWORD"));
    }

    @Test
    void givenKeycloakEmailFailure_WhenSendUpdatePasswordEmail_ThenThrowFriendlyException() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var usersResource = mock(UsersResource.class);
        var userResource = mock(UserResource.class);
        var gateway = new KeycloakUserActionGateway(keycloak, new KeycloakResilienceExecutor());

        when(keycloak.users()).thenReturn(usersResource);
        when(usersResource.get("user-1")).thenReturn(userResource);
        doThrow(new WebApplicationException(Response.status(500).build()))
                .when(userResource)
                .executeActionsEmail(List.of("UPDATE_PASSWORD"));

        var exception = assertThrows(WebApplicationException.class, () -> gateway.sendUpdatePasswordEmail("user-1"));

        assertEquals(500, exception.getResponse().getStatus());
        assertTrue(exception.getMessage().contains("Could not send the update password email. Check the SMTP configuration."));
    }
}
