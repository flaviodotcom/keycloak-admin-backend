package io.github.flaviodotcom.domain.identity.model;

import java.util.Map;

public record IdentityUserAttribute(
        String name,
        Map<String, String> displayName,
        Boolean insensitive,
        Boolean required,
        Boolean multivalued
) {

    public IdentityUserAttribute {
        displayName = displayName == null ? Map.of() : Map.copyOf(displayName);
    }
}
