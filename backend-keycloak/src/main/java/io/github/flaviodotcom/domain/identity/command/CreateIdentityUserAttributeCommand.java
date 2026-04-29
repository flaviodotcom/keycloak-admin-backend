package io.github.flaviodotcom.domain.identity.command;

import java.util.Map;

public record CreateIdentityUserAttributeCommand(
        String name,
        Map<String, String> displayName,
        Boolean insensitive,
        Boolean required,
        Boolean multivalued
) {

    public CreateIdentityUserAttributeCommand {
        displayName = displayName == null ? Map.of() : Map.copyOf(displayName);
        required = required == null ? Boolean.FALSE : required;
        multivalued = multivalued == null ? Boolean.FALSE : multivalued;
    }
}
