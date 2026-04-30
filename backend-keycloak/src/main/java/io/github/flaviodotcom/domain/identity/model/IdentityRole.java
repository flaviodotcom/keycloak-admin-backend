package io.github.flaviodotcom.domain.identity.model;

public record IdentityRole(
        String id,
        String name,
        String description,
        Boolean composite,
        Boolean clientRole,
        String containerId
) {
}
