package io.github.flaviodotcom.domain.identity.command;

import java.util.List;
import java.util.Map;

public record UpdateIdentityUserCommand(
        String username,
        String email,
        String firstName,
        String lastName,
        Boolean enabled,
        Boolean emailVerified,
        Map<String, List<String>> attributes
) {

    public UpdateIdentityUserCommand {
        enabled = enabled == null ? Boolean.TRUE : enabled;
        emailVerified = emailVerified == null ? Boolean.FALSE : emailVerified;
        attributes = attributes == null ? Map.of() : attributes;
    }

    public UpdateIdentityUserCommand withAttributes(Map<String, List<String>> attributes) {
        return new UpdateIdentityUserCommand(
                this.username,
                this.email,
                this.firstName,
                this.lastName,
                this.enabled,
                this.emailVerified,
                attributes
        );
    }
}
