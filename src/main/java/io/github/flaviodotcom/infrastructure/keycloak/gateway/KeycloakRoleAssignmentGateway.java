package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.gateway.IdentityRoleAssignmentGateway;
import io.github.flaviodotcom.exceptions.BusinessException;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakHttpResponseHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.AllArgsConstructor;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.List;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakRoleAssignmentGateway implements IdentityRoleAssignmentGateway {

    private final KeycloakAdminSupport keycloak;

    @Override
    public void assignRealmRoleToUser(String userId, String roleName) {
        this.execute(() -> this.userRoles(userId).realmLevel().add(List.of(this.realmRole(roleName))));
    }

    @Override
    public void unassignRealmRoleFromUser(String userId, String roleName) {
        this.execute(() -> this.userRoles(userId).realmLevel().remove(List.of(this.realmRole(roleName))));
    }

    @Override
    public void assignClientRoleToUser(String userId, String clientId, String roleName) {
        this.execute(() -> this.assignClientRole(this.userRoles(userId), clientId, roleName));
    }

    @Override
    public void unassignClientRoleFromUser(String userId, String clientId, String roleName) {
        this.execute(() -> this.unassignClientRole(this.userRoles(userId), clientId, roleName));
    }

    @Override
    public void assignRealmRoleToGroup(String groupId, String roleName) {
        this.execute(() -> this.groupRoles(groupId).realmLevel().add(List.of(this.realmRole(roleName))));
    }

    @Override
    public void unassignRealmRoleFromGroup(String groupId, String roleName) {
        this.execute(() -> this.groupRoles(groupId).realmLevel().remove(List.of(this.realmRole(roleName))));
    }

    @Override
    public void assignClientRoleToGroup(String groupId, String clientId, String roleName) {
        this.execute(() -> this.assignClientRole(this.groupRoles(groupId), clientId, roleName));
    }

    @Override
    public void unassignClientRoleFromGroup(String groupId, String clientId, String roleName) {
        this.execute(() -> this.unassignClientRole(this.groupRoles(groupId), clientId, roleName));
    }

    private void assignClientRole(RoleMappingResource roles, String clientId, String roleName) {
        var role = this.clientRole(clientId, roleName);
        roles.clientLevel(role.clientUuid()).add(List.of(role.representation()));
    }

    private void unassignClientRole(RoleMappingResource roles, String clientId, String roleName) {
        var role = this.clientRole(clientId, roleName);
        roles.clientLevel(role.clientUuid()).remove(List.of(role.representation()));
    }

    private RoleMappingResource userRoles(String userId) {
        return this.keycloak.users().get(userId).roles();
    }

    private RoleMappingResource groupRoles(String groupId) {
        return this.keycloak.groups().group(groupId).roles();
    }

    private RoleRepresentation realmRole(String roleName) {
        return this.keycloak.roles().get(roleName).toRepresentation();
    }

    private KeycloakClientRole clientRole(String clientId, String roleName) {
        var clientUuid = this.clientUuid(clientId);
        var role = this.keycloak.realm().clients().get(clientUuid).roles().get(roleName).toRepresentation();
        return new KeycloakClientRole(clientUuid, role);
    }

    private String clientUuid(String clientId) {
        var clients = this.keycloak.realm().clients().findByClientId(clientId);
        if (clients.isEmpty()) {
            throw BusinessException.localized("error.keycloak.client.not-found", clientId);
        }
        if (clients.size() > 1) {
            throw BusinessException.localized("error.keycloak.client.ambiguous", clientId);
        }

        return clients.getFirst().getId();
    }

    private void execute(KeycloakOperation operation) {
        try {
            operation.run();
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    private record KeycloakClientRole(String clientUuid, RoleRepresentation representation) {
    }

    @FunctionalInterface
    private interface KeycloakOperation {

        void run();
    }
}
