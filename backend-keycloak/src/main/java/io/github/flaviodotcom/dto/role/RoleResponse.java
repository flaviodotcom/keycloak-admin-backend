package io.github.flaviodotcom.dto.role;

import io.github.flaviodotcom.domain.identity.model.IdentityRole;
import io.github.flaviodotcom.infrastructure.interception.contracts.ActionPayload;

import java.util.Map;

public record RoleResponse(
        String id,
        String name,
        String description,
        Boolean composite,
        Boolean clientRole,
        String containerId
) implements ActionPayload {

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

    @Override
    public String actionSubjectId() {
        return this.id;
    }

    @Override
    public Map<String, Object> actionMetadata() {
        return Map.of(
                "name",
                this.name
        );
    }
}
