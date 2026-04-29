package io.github.flaviodotcom.domain.identity.model;

import java.util.List;
import java.util.Map;

public record IdentityUser(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        Boolean enabled,
        Boolean emailVerified,
        Long createdTimestamp,
        Map<String, List<String>> attributes
) {

    public IdentityUser {
        attributes = attributes == null ? Map.of() : attributes;
    }
}
