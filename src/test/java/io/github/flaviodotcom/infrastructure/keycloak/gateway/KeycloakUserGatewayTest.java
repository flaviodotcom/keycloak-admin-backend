package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.infrastructure.keycloak.candidate.KeycloakUserCandidateFinder;
import io.github.flaviodotcom.infrastructure.keycloak.mapper.KeycloakRepresentationMapper;
import io.github.flaviodotcom.infrastructure.keycloak.matcher.KeycloakUserMatcher;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KeycloakUserGatewayTest {

    @Test
    void givenSearchAndEnabledFilter_WhenFindUsers_ThenSearchSupportedFieldsAndApplyLocalFilters() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var usersResource = usersResource();
        var gateway = gateway(keycloak);
        var maria = user("user-1", "maria.teste", "maria.teste@email.com", "Mária", "Teste", true);
        var disabledMaria = user("user-2", "maria.inativa", "maria.inativa@email.com", "Maria", "Inativa", false);

        when(keycloak.users()).thenReturn(usersResource);
        when(usersResource.searchByFirstName("Maria", false)).thenReturn(List.of(maria, disabledMaria));

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
    void givenAccentInsensitiveAttributeFilter_WhenFindUsers_ThenReturnMatchingUser() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var usersResource = usersResource();
        var gateway = gateway(keycloak);
        var maria = user("user-1", "maria.teste", "maria.teste@email.com", "Maria", "Teste", true);
        maria.setAttributes(Map.of("departamento", List.of("Recursos Humanos")));

        when(keycloak.users()).thenReturn(usersResource);
        when(usersResource.searchByAttributes("departamento:recursos humanos", false)).thenReturn(List.of(maria));

        var users = gateway.findUsers(new UserSearchCriteria(
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                Map.of("departamento", "recursos humanos")
        ));

        assertEquals(1, users.size());
        assertEquals("user-1", users.getFirst().id());
    }

    @Test
    void givenStructuredAccentFilter_WhenFindUsers_ThenNormalizeKeycloakFilterAndApplyLocalFilter() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var usersResource = usersResource();
        var gateway = gateway(keycloak);
        var maria = user("user-1", "maria.teste", "maria.teste@email.com", "Maria", "José", true);

        when(keycloak.users()).thenReturn(usersResource);
        when(usersResource.searchByLastName("José", false)).thenReturn(List.of(maria));

        var users = gateway.findUsers(new UserSearchCriteria(
                null,
                null,
                null,
                null,
                "José",
                null,
                false,
                Map.of()
        ));

        assertEquals(1, users.size());
        assertEquals("maria.teste", users.getFirst().username());
    }

    @Test
    void givenSearchByAccentLastName_WhenFindUsers_ThenSearchSupportedFieldsAndReturnMatchingUser() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var usersResource = usersResource();
        var gateway = gateway(keycloak);
        var jose = user("user-1", "jose.teste", "jose.teste@email.com", "José", "Conceição Teste", true);

        when(keycloak.users()).thenReturn(usersResource);
        when(usersResource.searchByLastName("Conceição", false)).thenReturn(List.of(jose));

        var users = gateway.findUsers(new UserSearchCriteria(
                "Conceição",
                null,
                null,
                null,
                null,
                null,
                false,
                Map.of()
        ));

        assertEquals(1, users.size());
        assertEquals("jose.teste", users.getFirst().username());
    }

    @Test
    void givenUnaccentedFirstNameFilter_WhenFindUsers_ThenSearchCandidateTermsAndReturnAccentMatchingUser() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var usersResource = usersResource();
        var gateway = gateway(keycloak);
        var jose = user("user-1", "jose.teste", "jose.teste@email.com", "José", "Conceição Teste", true);

        when(keycloak.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername("jose", false)).thenReturn(List.of(jose));

        var users = gateway.findUsers(new UserSearchCriteria(
                null,
                null,
                null,
                "jose",
                null,
                null,
                false,
                Map.of()
        ));

        assertEquals(1, users.size());
        assertEquals("José", users.getFirst().firstName());
    }

    @Test
    void givenFirstNameAndLastNameFilters_WhenFindUsers_ThenReturnUserMatchingBothFilters() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var usersResource = usersResource();
        var gateway = gateway(keycloak);
        var jose = user("user-1", "jose.teste", "jose.teste@email.com", "José", "Conceição Teste", true);
        var otherJose = user("user-2", "jose.outro", "jose.outro@email.com", "José", "Outro", true);

        when(keycloak.users()).thenReturn(usersResource);
        when(usersResource.searchByFirstName("José", false)).thenReturn(List.of(jose, otherJose));

        var users = gateway.findUsers(new UserSearchCriteria(
                null,
                null,
                null,
                "José",
                "Conceição Teste",
                null,
                false,
                Map.of()
        ));

        assertEquals(1, users.size());
        assertEquals("jose.teste", users.getFirst().username());
    }

    private UsersResource usersResource() {
        return mock(UsersResource.class, invocation -> {
            if (List.class.equals(invocation.getMethod().getReturnType())) {
                return List.of();
            }

            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        });
    }

    private KeycloakUserGateway gateway(KeycloakAdminSupport keycloak) {
        return new KeycloakUserGateway(
                keycloak,
                new KeycloakUserCandidateFinder(keycloak),
                new KeycloakRepresentationMapper(),
                new KeycloakUserMatcher()
        );
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
