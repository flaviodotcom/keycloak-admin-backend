package io.github.flaviodotcom.infrastructure.keycloak.userprofile;

import io.github.flaviodotcom.domain.identity.model.IdentityUserAttribute;
import io.github.flaviodotcom.exceptions.BusinessException;

import java.util.Map;

public record KeycloakUserAttributeDefinitions(Map<String, IdentityUserAttribute> attributes) {

    public KeycloakUserAttributeDefinitions {
        attributes = Map.copyOf(attributes);
    }

    public IdentityUserAttribute get(String name) {
        var definition = this.attributes.get(name);
        if (definition == null) {
            throw BusinessException.localized("error.user-attribute.not-configured", name);
        }
        return definition;
    }

    public boolean isInsensitive(String name) {
        return Boolean.TRUE.equals(this.get(name).insensitive());
    }
}
