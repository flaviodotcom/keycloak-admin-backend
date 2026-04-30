package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import io.github.flaviodotcom.infrastructure.keycloak.resilience.KeycloakResilienceExecutor;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KeycloakUserPostCreationGatewayTest {

    @Test
    void givenGroupIds_WhenAssignGroups_ThenJoinEachGroup() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var usersResource = mock(UsersResource.class);
        var userResource = mock(UserResource.class);
        var gateway = new KeycloakUserPostCreationGateway(keycloak, new KeycloakResilienceExecutor());

        when(keycloak.users()).thenReturn(usersResource);
        when(usersResource.get("user-1")).thenReturn(userResource);

        gateway.assignGroups("user-1", List.of("group-1", "group-2"));

        verify(userResource).joinGroup("group-1");
        verify(userResource).joinGroup("group-2");
    }
}
