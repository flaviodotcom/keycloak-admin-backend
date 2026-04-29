package io.github.flaviodotcom.domain.identity.gateway;

import io.github.flaviodotcom.domain.identity.model.IdentityUserSession;

import java.util.List;

public interface IdentitySessionGateway {

    List<IdentityUserSession> findUserSessions(String userId);

    void logoutUser(String userId);

    void deleteUserSession(String userId, String sessionId);
}
