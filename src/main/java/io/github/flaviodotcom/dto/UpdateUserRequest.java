package io.github.flaviodotcom.dto;

import io.github.flaviodotcom.domain.identity.command.UpdateIdentityUserCommand;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

public record UpdateUserRequest(
        @NotBlank(message = "username is required")
        String username,
        String email,
        String firstName,
        String lastName,
        Boolean enabled,
        Boolean emailVerified,
        Map<String, List<String>> attributes
) {

    public UpdateUserRequest {
        enabled = enabled == null ? Boolean.TRUE : enabled;
        emailVerified = emailVerified == null ? Boolean.FALSE : emailVerified;
        attributes = attributes == null ? Map.of() : attributes;
    }

    public UpdateIdentityUserCommand toCommand() {
        return new UpdateIdentityUserCommand(
                this.username,
                this.email,
                this.firstName,
                this.lastName,
                this.enabled,
                this.emailVerified,
                this.attributes
        );
    }
}
