package io.github.flaviodotcom.domain.identity.model;

import java.util.Map;

public record IdentityUserSession(
        String id,
        String userId,
        String username,
        String ipAddress,
        long start,
        long lastAccess,
        boolean rememberMe,
        Map<String, String> clients
) {

    public IdentityUserSession {
        clients = clients == null ? Map.of() : Map.copyOf(clients);
    }
}
