package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.command.UpdateIdentityUserCommand;
import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.domain.identity.gateway.IdentityUserAttributeGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityUserAttribute;
import io.github.flaviodotcom.infrastructure.keycloak.candidate.KeycloakUserCandidateFinder;
import io.github.flaviodotcom.infrastructure.keycloak.mapper.KeycloakRepresentationMapper;
import io.github.flaviodotcom.infrastructure.keycloak.matcher.KeycloakUserMatcher;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakUserAttributeIndex;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KeycloakUserGatewayTest {

    @Test
    void givenSearchAndEnabledFilter_WhenFindUsers_ThenSearchSupportedFieldsAndApplyLocalFilters() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var usersResource = usersResource();
        var gateway = gateway(keycloak, mock(IdentityUserAttributeGateway.class));
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
        var attributeGateway = mock(IdentityUserAttributeGateway.class);
        var usersResource = usersResource();
        var gateway = gateway(keycloak, attributeGateway);
        var maria = user("user-1", "maria.teste", "maria.teste@email.com", "Maria", "Teste", true);
        maria.setAttributes(Map.of("departamento", List.of("Recursos Humanos")));

        when(keycloak.users()).thenReturn(usersResource);
        when(attributeGateway.findAttribute("departamento")).thenReturn(new IdentityUserAttribute(
                "departamento",
                Map.of(),
                true,
                false,
                false
        ));
        when(usersResource.searchByAttributes("__search_departamento:recursos humanos", false)).thenReturn(List.of(maria));

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
    void givenLegacyAttributeFilter_WhenFindUsers_ThenSearchByPublicAttributeName() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var attributeGateway = mock(IdentityUserAttributeGateway.class);
        var usersResource = usersResource();
        var gateway = gateway(keycloak, attributeGateway);
        var user = user("user-1", "marcos.teste", "marcos.teste@email.com", "Marcos", "Teste", true);
        user.setAttributes(Map.of("cpf", List.of("12345678903")));

        when(keycloak.users()).thenReturn(usersResource);
        when(attributeGateway.findAttribute("cpf")).thenReturn(new IdentityUserAttribute(
                "cpf",
                Map.of(),
                false,
                false,
                false
        ));
        when(usersResource.searchByAttributes("cpf:12345678903", false)).thenReturn(List.of(user));

        var users = gateway.findUsers(new UserSearchCriteria(
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                Map.of("cpf", "12345678903")
        ));

        assertEquals(1, users.size());
        assertEquals("marcos.teste", users.getFirst().username());
    }

    @Test
    void givenStructuredAccentFilter_WhenFindUsers_ThenNormalizeKeycloakFilterAndApplyLocalFilter() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var usersResource = usersResource();
        var gateway = gateway(keycloak, mock(IdentityUserAttributeGateway.class));
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
        var gateway = gateway(keycloak, mock(IdentityUserAttributeGateway.class));
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
        var gateway = gateway(keycloak, mock(IdentityUserAttributeGateway.class));
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
        var gateway = gateway(keycloak, mock(IdentityUserAttributeGateway.class));
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

    @Test
    void givenInsensitiveAttribute_WhenUpdateUser_ThenPersistInternalSearchAttribute() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var attributeGateway = mock(IdentityUserAttributeGateway.class);
        var usersResource = usersResource();
        var userResource = mock(UserResource.class);
        var gateway = gateway(keycloak, attributeGateway);
        var updatedUser = user("user-1", "pedro.teste", "pedro.teste@email.com", "Pedro", "Teste", true);
        updatedUser.setAttributes(Map.of(
                "name", List.of("Pedro Paulo Timbo Teste"),
                "__search_name", List.of("pedro paulo timbo teste")
        ));

        when(keycloak.users()).thenReturn(usersResource);
        when(usersResource.get("user-1")).thenReturn(userResource);
        when(attributeGateway.findAttribute("name")).thenReturn(new IdentityUserAttribute(
                "name",
                Map.of(),
                true,
                false,
                false
        ));
        when(userResource.toRepresentation()).thenReturn(updatedUser);

        var user = gateway.updateUser("user-1", new UpdateIdentityUserCommand(
                "pedro.teste",
                "pedro.teste@email.com",
                "Pedro",
                "Teste",
                true,
                false,
                Map.of("name", List.of("Pedro Paulo Timbó Teste"))
        ));

        assertEquals("pedro.teste", user.username());
        verify(userResource).update(argThat(representation ->
                List.of("pedro paulo timbo teste").equals(representation.getAttributes().get("__search_name"))
        ));
    }

    private UsersResource usersResource() {
        return mock(UsersResource.class, invocation -> {
            if (List.class.equals(invocation.getMethod().getReturnType())) {
                return List.of();
            }

            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        });
    }

    private KeycloakUserGateway gateway(KeycloakAdminSupport keycloak, IdentityUserAttributeGateway attributeGateway) {
        var attributeIndex = new KeycloakUserAttributeIndex(attributeGateway);
        return new KeycloakUserGateway(
                keycloak,
                new KeycloakUserCandidateFinder(keycloak, attributeIndex),
                new KeycloakRepresentationMapper(),
                new KeycloakUserMatcher(attributeIndex),
                attributeIndex
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
