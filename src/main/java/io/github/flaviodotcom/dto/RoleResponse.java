package io.github.flaviodotcom.dto;

import io.github.flaviodotcom.domain.identity.model.IdentityRole;

public record RoleResponse(
        String id,
        String name,
        String description,
        Boolean composite,
        Boolean clientRole,
        String containerId
) {

    public static RoleResponse fromIdentityRole(IdentityRole identityRole) {
        return new RoleResponse(
                identityRole.id(),
                identityRole.name(),
                identityRole.description(),
                identityRole.composite(),
                identityRole.clientRole(),
                identityRole.containerId()
        );
    }
}
