package io.github.flaviodotcom.domain.identity.command;

public record CreateIdentityRoleCommand(
        String name,
        String description
) {
}
