package io.github.flaviodotcom.domain.identity;

public record IdentityRole(
        String id,
        String name,
        String description,
        Boolean composite,
        Boolean clientRole,
        String containerId
) {
}
