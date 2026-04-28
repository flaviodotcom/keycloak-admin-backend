package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.gateway.IdentityMembershipGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityGroup;
import io.github.flaviodotcom.domain.identity.model.IdentityUser;
import io.github.flaviodotcom.infrastructure.keycloak.mapper.KeycloakRepresentationMapper;
import io.github.flaviodotcom.infrastructure.keycloak.resilience.KeycloakResilienceExecutor;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakHttpResponseHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.AllArgsConstructor;
import org.keycloak.admin.client.resource.UsersResource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.github.flaviodotcom.infrastructure.keycloak.pagination.KeycloakQueryDefaults.FIRST_RESULT;
import static io.github.flaviodotcom.infrastructure.keycloak.pagination.KeycloakQueryDefaults.MAX_RESULTS;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakIdentityMembershipGateway implements IdentityMembershipGateway {

    private final KeycloakAdminSupport keycloak;
    private final KeycloakRepresentationMapper mapper;
    private final KeycloakResilienceExecutor resilience;

    @Override
    public List<IdentityGroup> findUserGroups(String userId) {
        return this.resilience.executeRead(() -> {
            try {
                return this.findUserGroups(this.keycloak.users(), userId);
            } catch (WebApplicationException exception) {
                throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
            }
        });
    }

    @Override
    public Map<String, List<IdentityGroup>> findUsersGroups(List<String> userIds) {
        return this.resilience.executeRead(() -> {
            try {
                var usersResource = this.keycloak.users();
                var groupsByUserId = new LinkedHashMap<String, List<IdentityGroup>>();
                for (var userId : userIds) {
                    groupsByUserId.put(userId, this.findUserGroups(usersResource, userId));
                }
                return Map.copyOf(groupsByUserId);
            } catch (WebApplicationException exception) {
                throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
            }
        });
    }

    @Override
    public void assignUserToGroup(String userId, String groupId) {
        this.resilience.executeWrite(() -> {
            try {
                this.keycloak.users().get(userId).joinGroup(groupId);
            } catch (WebApplicationException exception) {
                throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
            }
        });
    }

    @Override
    public void unassignUserFromGroup(String userId, String groupId) {
        this.resilience.executeWrite(() -> {
            try {
                this.keycloak.users().get(userId).leaveGroup(groupId);
            } catch (WebApplicationException exception) {
                throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
            }
        });
    }

    @Override
    public List<IdentityUser> findGroupMembers(String groupId) {
        return this.resilience.executeRead(() -> {
            try {
                return this.keycloak.groups().group(groupId).members(FIRST_RESULT, MAX_RESULTS).stream()
                        .map(this.mapper::toIdentityUser)
                        .toList();
            } catch (WebApplicationException exception) {
                throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
            }
        });
    }

    private List<IdentityGroup> findUserGroups(UsersResource usersResource, String userId) {
        return usersResource.get(userId).groups().stream()
                .map(this.mapper::toIdentityGroup)
                .toList();
    }
}
