package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.github.flaviodotcom.infrastructure.keycloak.candidate.KeycloakRoleCandidateFinder;
import io.github.flaviodotcom.infrastructure.keycloak.mapper.KeycloakRepresentationMapper;
import io.github.flaviodotcom.infrastructure.keycloak.matcher.KeycloakRoleMatcher;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import org.mockito.Mockito;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KeycloakRoleGatewayTest {

    @Test
    void givenRoleNameFilter_WhenFindRoles_ThenApplyCaseAndAccentInsensitiveFilter() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var rolesResource = rolesResource();
        var gateway = gateway(keycloak);
        var role = role("role-1", "Gestão");

        when(keycloak.roles()).thenReturn(rolesResource);
        when(rolesResource.list("gestao", 0, Integer.MAX_VALUE, false)).thenReturn(List.of(role));

        var roles = gateway.findRoles(new RoleSearchCriteria("gestao", false));

        assertEquals(1, roles.size());
        assertEquals("Gestão", roles.getFirst().name());
    }

    private RoleRepresentation role(String id, String name) {
        var role = new RoleRepresentation();
        role.setId(id);
        role.setName(name);
        return role;
    }

    private RolesResource rolesResource() {
        return mock(RolesResource.class, invocation -> {
            if (List.class.equals(invocation.getMethod().getReturnType())) {
                return List.of();
            }

            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        });
    }

    private KeycloakRoleGateway gateway(KeycloakAdminSupport keycloak) {
        return new KeycloakRoleGateway(
                keycloak,
                new KeycloakRoleCandidateFinder(keycloak),
                new KeycloakRepresentationMapper(),
                new KeycloakRoleMatcher()
        );
    }
}
