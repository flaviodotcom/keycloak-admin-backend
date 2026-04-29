package io.github.flaviodotcom.domain.identity.command;

import java.util.List;
import java.util.Map;

public record PatchIdentityUserCommand(
        String username,
        String email,
        String firstName,
        String lastName,
        Boolean enabled,
        Boolean emailVerified,
        Map<String, List<String>> attributes
) {
}
