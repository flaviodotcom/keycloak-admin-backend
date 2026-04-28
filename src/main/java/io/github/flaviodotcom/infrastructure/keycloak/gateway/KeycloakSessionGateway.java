package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.gateway.IdentitySessionGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityUserSession;
import io.github.flaviodotcom.exceptions.BusinessException;
import io.github.flaviodotcom.infrastructure.keycloak.mapper.KeycloakRepresentationMapper;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakHttpResponseHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.AllArgsConstructor;

import java.util.List;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakSessionGateway implements IdentitySessionGateway {

    private final KeycloakAdminSupport keycloak;
    private final KeycloakRepresentationMapper mapper;

    @Override
    public List<IdentityUserSession> findUserSessions(String userId) {
        try {
            return this.keycloak.users().get(userId).getUserSessions().stream()
                    .map(this.mapper::toIdentityUserSession)
                    .toList();
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    @Override
    public void logoutUser(String userId) {
        try {
            this.keycloak.users().get(userId).logout();
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    @Override
    public void deleteUserSession(String userId, String sessionId) {
        try {
            var sessionExists = this.keycloak.users().get(userId).getUserSessions().stream()
                    .anyMatch(session -> sessionId.equals(session.getId()));
            if (!sessionExists) {
                throw BusinessException.localized("error.keycloak.user-session.not-found", userId, sessionId);
            }

            this.keycloak.realm().deleteSession(sessionId, false);
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }
}
