package io.github.flaviodotcom.dto.user;

import io.github.flaviodotcom.domain.identity.model.IdentityUserSession;

import java.util.Map;

public record UserSessionResponse(
        String id,
        String userId,
        String username,
        String ipAddress,
        long start,
        long lastAccess,
        boolean rememberMe,
        Map<String, String> clients
) {

    public static UserSessionResponse fromIdentityUserSession(IdentityUserSession session) {
        return new UserSessionResponse(
                session.id(),
                session.userId(),
                session.username(),
                session.ipAddress(),
                session.start(),
                session.lastAccess(),
                session.rememberMe(),
                session.clients()
        );
    }
}
