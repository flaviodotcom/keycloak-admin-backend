package io.github.flaviodotcom.domain.identity.command;

public record UpdateIdentityRoleCommand(
        String name,
        String description
) {
}
