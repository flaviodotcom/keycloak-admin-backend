package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityRoleCommand;
import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.github.flaviodotcom.domain.identity.gateway.IdentityRoleGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityRole;
import io.github.flaviodotcom.infrastructure.keycloak.mapper.KeycloakRepresentationMapper;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakFilterMatcher;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakHttpResponseHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.AllArgsConstructor;

import java.util.List;

import static io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakQueryDefaults.FIRST_RESULT;
import static io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakQueryDefaults.MAX_RESULTS;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakRoleGateway implements IdentityRoleGateway {

    private final KeycloakAdminSupport keycloak;
    private final KeycloakRepresentationMapper mapper;

    @Override
    public List<IdentityRole> findRoles(RoleSearchCriteria criteria) {
        try {
            var roles = criteria.name() == null
                    ? this.keycloak.roles().list(FIRST_RESULT, MAX_RESULTS, false)
                    : this.keycloak.roles().list(criteria.name(), FIRST_RESULT, MAX_RESULTS, false);

            return roles.stream()
                    .map(this.mapper::toIdentityRole)
                    .filter(role -> this.matches(role, criteria))
                    .toList();
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    @Override
    public IdentityRole createRole(CreateIdentityRoleCommand command) {
        try {
            this.keycloak.roles().create(this.mapper.toRoleRepresentation(command));
            return this.mapper.toIdentityRole(this.keycloak.roles().get(command.name()).toRepresentation());
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    private boolean matches(IdentityRole role, RoleSearchCriteria criteria) {
        return KeycloakFilterMatcher.matchesText(criteria.name(), role.name(), criteria.exact());
    }
}
