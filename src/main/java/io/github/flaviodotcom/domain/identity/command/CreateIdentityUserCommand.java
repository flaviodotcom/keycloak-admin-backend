package io.github.flaviodotcom.domain.identity.command;

import java.util.List;
import java.util.Map;

public record CreateIdentityUserCommand(
        String username,
        String email,
        String firstName,
        String lastName,
        Boolean enabled,
        Boolean emailVerified,
        Map<String, List<String>> attributes,
        List<String> groupIds
) {

    public CreateIdentityUserCommand {
        enabled = enabled == null ? Boolean.TRUE : enabled;
        emailVerified = emailVerified == null ? Boolean.FALSE : emailVerified;
        attributes = attributes == null ? Map.of() : attributes;
        groupIds = groupIds == null ? List.of() : List.copyOf(groupIds);
    }

    public CreateIdentityUserCommand withAttributes(Map<String, List<String>> attributes) {
        return new CreateIdentityUserCommand(
                this.username,
                this.email,
                this.firstName,
                this.lastName,
                this.enabled,
                this.emailVerified,
                attributes,
                this.groupIds
        );
    }
}
