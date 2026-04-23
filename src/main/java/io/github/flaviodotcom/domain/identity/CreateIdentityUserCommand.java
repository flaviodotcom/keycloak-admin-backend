package io.github.flaviodotcom.domain.identity;

import java.util.List;
import java.util.Map;

public record CreateIdentityUserCommand(
        String username,
        String email,
        String firstName,
        String lastName,
        Boolean enabled,
        Boolean emailVerified,
        Map<String, List<String>> attributes
) {

    public CreateIdentityUserCommand {
        enabled = enabled == null ? Boolean.TRUE : enabled;
        emailVerified = emailVerified == null ? Boolean.FALSE : emailVerified;
        attributes = attributes == null ? Map.of() : attributes;
    }
}
