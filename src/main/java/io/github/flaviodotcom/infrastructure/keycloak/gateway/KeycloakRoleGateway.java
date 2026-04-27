package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityRoleCommand;
import io.github.flaviodotcom.domain.identity.command.UpdateIdentityRoleCommand;
import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.github.flaviodotcom.domain.identity.gateway.IdentityRoleGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityRole;
import io.github.flaviodotcom.infrastructure.keycloak.candidate.KeycloakRoleCandidateFinder;
import io.github.flaviodotcom.infrastructure.keycloak.mapper.KeycloakRepresentationMapper;
import io.github.flaviodotcom.infrastructure.keycloak.matcher.KeycloakRoleMatcher;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakHttpResponseHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.AllArgsConstructor;

import java.util.List;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakRoleGateway implements IdentityRoleGateway {

    private final KeycloakAdminSupport keycloak;
    private final KeycloakRoleCandidateFinder candidateFinder;
    private final KeycloakRepresentationMapper mapper;
    private final KeycloakRoleMatcher matcher;

    @Override
    public List<IdentityRole> findRoles(RoleSearchCriteria criteria) {
        try {
            return this.candidateFinder.findCandidates(criteria).stream()
                    .map(this.mapper::toIdentityRole)
                    .filter(role -> this.matcher.matches(role, criteria))
                    .toList();
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    @Override
    public IdentityRole findRoleById(String id) {
        try {
            return this.mapper.toIdentityRole(this.keycloak.rolesById().getRole(id));
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

    @Override
    public IdentityRole updateRole(String id, UpdateIdentityRoleCommand command) {
        try {
            this.keycloak.rolesById().updateRole(id, this.mapper.toRoleRepresentation(id, command));
            return this.mapper.toIdentityRole(this.keycloak.rolesById().getRole(id));
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    @Override
    public void deleteRole(String id) {
        try {
            this.keycloak.rolesById().deleteRole(id);
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }
}
