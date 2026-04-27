package io.github.flaviodotcom.infrastructure.keycloak.support;

import io.github.flaviodotcom.exceptions.LocalizedIllegalStateException;
import io.github.flaviodotcom.i18n.Messages;
import jakarta.ws.rs.core.Response;

public final class CreatedResourceLocation {

    private CreatedResourceLocation() {
    }

    public static String extractId(Response response) {
        var location = response.getLocation();
        if (location == null) {
            throw localizedException("error.keycloak.created-location.missing");
        }

        var path = location.getPath();
        if (path == null || path.isBlank()) {
            throw localizedException("error.keycloak.created-location.empty");
        }

        var resourceId = path.substring(path.lastIndexOf('/') + 1).strip();
        if (resourceId.isBlank()) {
            throw localizedException("error.keycloak.created-location.invalid");
        }

        return resourceId;
    }

    private static LocalizedIllegalStateException localizedException(String messageKey) {
        return new LocalizedIllegalStateException(messageKey, Messages.getDefault(messageKey));
    }
}
