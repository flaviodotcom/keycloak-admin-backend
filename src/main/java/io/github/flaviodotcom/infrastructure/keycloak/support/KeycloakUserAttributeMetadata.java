package io.github.flaviodotcom.infrastructure.keycloak.support;

import java.util.Set;

public final class KeycloakUserAttributeMetadata {

    public static final String INSENSITIVE_ANNOTATION = "identity.insensitive";
    public static final String INTERNAL_ANNOTATION = "identity.internal";
    public static final String SOURCE_ATTRIBUTE_ANNOTATION = "identity.sourceAttribute";
    public static final Set<String> ADMIN_USER_ROLES = Set.of("admin", "user");
    public static final Set<String> ADMIN_ONLY_ROLE = Set.of("admin");

    private KeycloakUserAttributeMetadata() {
    }
}
