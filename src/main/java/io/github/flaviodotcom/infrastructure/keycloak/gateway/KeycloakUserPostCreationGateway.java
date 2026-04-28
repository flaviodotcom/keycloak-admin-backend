package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.gateway.IdentityUserPostCreationGateway;
import io.github.flaviodotcom.infrastructure.keycloak.resilience.KeycloakResilienceExecutor;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakHttpResponseHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.AllArgsConstructor;

import java.util.List;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakUserPostCreationGateway implements IdentityUserPostCreationGateway {

    private final KeycloakAdminSupport keycloak;
    private final KeycloakResilienceExecutor resilience;

    @Override
    public void assignGroups(String userId, List<String> groupIds) {
        this.resilience.executeWrite(() -> {
            try {
                var userResource = this.keycloak.users().get(userId);
                for (var groupId : groupIds) {
                    userResource.joinGroup(groupId);
                }
            } catch (WebApplicationException exception) {
                throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
            }
        });
    }
}
