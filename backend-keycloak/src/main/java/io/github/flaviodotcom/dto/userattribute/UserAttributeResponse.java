package io.github.flaviodotcom.dto.userattribute;

import io.github.flaviodotcom.domain.identity.model.IdentityUserAttribute;

import java.util.Map;

public record UserAttributeResponse(
        String name,
        Map<String, String> displayName,
        Boolean insensitive,
        Boolean required,
        Boolean multivalued
) {

    public static UserAttributeResponse fromIdentityUserAttribute(IdentityUserAttribute attribute) {
        return new UserAttributeResponse(
                attribute.name(),
                attribute.displayName(),
                attribute.insensitive(),
                attribute.required(),
                attribute.multivalued()
        );
    }
}
