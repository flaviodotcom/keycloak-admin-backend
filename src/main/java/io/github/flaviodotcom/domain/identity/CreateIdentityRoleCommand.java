package io.github.flaviodotcom.domain.identity;

public record CreateIdentityRoleCommand(
        String name,
        String description
) {
}
