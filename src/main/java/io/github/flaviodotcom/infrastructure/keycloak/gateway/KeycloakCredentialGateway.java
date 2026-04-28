package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.gateway.IdentityCredentialGateway;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakHttpResponseHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.AllArgsConstructor;
import org.keycloak.representations.idm.CredentialRepresentation;

import java.util.List;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakCredentialGateway implements IdentityCredentialGateway {

    private final KeycloakAdminSupport keycloak;

    @Override
    public void resetPassword(String userId, String value, boolean temporary) {
        try {
            var credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(value);
            credential.setTemporary(temporary);
            this.keycloak.users().get(userId).resetPassword(credential);
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    @Override
    public void updateRequiredActions(String userId, List<String> actions) {
        try {
            var userResource = this.keycloak.users().get(userId);
            var representation = userResource.toRepresentation();
            representation.setRequiredActions(List.copyOf(actions));
            userResource.update(representation);
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }
}
