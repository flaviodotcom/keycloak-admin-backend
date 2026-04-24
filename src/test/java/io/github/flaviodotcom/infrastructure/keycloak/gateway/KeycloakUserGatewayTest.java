package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.infrastructure.keycloak.mapper.KeycloakRepresentationMapper;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KeycloakUserGatewayTest {

    @Test
    void givenSearchAndEnabledFilter_WhenFindUsers_ThenSearchSupportedFieldsAndApplyLocalFilters() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var usersResource = mock(UsersResource.class);
        var gateway = new KeycloakUserGateway(keycloak, new KeycloakRepresentationMapper());
        var maria = user("user-1", "maria.teste", "maria.teste@email.com", "Maria", "Teste", true);
        var disabledMaria = user("user-2", "maria.inativa", "maria.inativa@email.com", "Maria", "Inativa", false);

        when(keycloak.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername("Maria", false)).thenReturn(List.of());
        when(usersResource.searchByEmail("Maria", false)).thenReturn(List.of());
        when(usersResource.searchByFirstName("Maria", false)).thenReturn(List.of(maria, disabledMaria));
        when(usersResource.searchByLastName("Maria", false)).thenReturn(List.of());

        var users = gateway.findUsers(new UserSearchCriteria(
                "Maria",
                null,
                null,
                null,
                null,
                true,
                false,
                Map.of()
        ));

        assertEquals(1, users.size());
        assertEquals("maria.teste", users.getFirst().username());
    }

    @Test
    void givenUserMatchingMoreThanOneSearchField_WhenFindUsers_ThenReturnUserOnlyOnce() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var usersResource = mock(UsersResource.class);
        var gateway = new KeycloakUserGateway(keycloak, new KeycloakRepresentationMapper());
        var maria = user("user-1", "maria.teste", "maria.teste@email.com", "Maria", "Maria", true);

        when(keycloak.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername("Maria", false)).thenReturn(List.of(maria));
        when(usersResource.searchByEmail("Maria", false)).thenReturn(List.of());
        when(usersResource.searchByFirstName("Maria", false)).thenReturn(List.of(maria));
        when(usersResource.searchByLastName("Maria", false)).thenReturn(List.of(maria));

        var users = gateway.findUsers(new UserSearchCriteria(
                "Maria",
                null,
                null,
                null,
                null,
                null,
                false,
                Map.of()
        ));

        assertEquals(1, users.size());
        assertEquals("user-1", users.getFirst().id());
    }

    private UserRepresentation user(
            String id,
            String username,
            String email,
            String firstName,
            String lastName,
            Boolean enabled
    ) {
        var user = new UserRepresentation();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(enabled);
        return user;
    }
}
