package io.github.flaviodotcom.infrastructure.keycloak;

import jakarta.ws.rs.core.Response;

public final class CreatedResourceLocation {

    private CreatedResourceLocation() {
    }

    public static String extractId(Response response) {
        var location = response.getLocation();
        if (location == null) {
            throw new IllegalStateException("Keycloak did not return a location header for the created resource.");
        }

        var path = location.getPath();
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("Keycloak returned an empty location header for the created resource.");
        }

        var resourceId = path.substring(path.lastIndexOf('/') + 1).strip();
        if (resourceId.isBlank()) {
            throw new IllegalStateException("Keycloak returned an invalid location header for the created resource.");
        }

        return resourceId;
    }
}
