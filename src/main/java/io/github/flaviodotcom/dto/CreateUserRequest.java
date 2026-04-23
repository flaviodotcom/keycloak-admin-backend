package io.github.flaviodotcom.dto;

import io.github.flaviodotcom.domain.identity.CreateIdentityUserCommand;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

public record CreateUserRequest(
        @NotBlank(message = "username is required")
        String username,
        String email,
        String firstName,
        String lastName,
        Boolean enabled,
        Boolean emailVerified,
        Map<String, List<String>> attributes
) {

    public CreateUserRequest {
        enabled = enabled == null ? Boolean.TRUE : enabled;
        emailVerified = emailVerified == null ? Boolean.FALSE : emailVerified;
        attributes = attributes == null ? Map.of() : attributes;
    }

    public CreateIdentityUserCommand toCommand() {
        return new CreateIdentityUserCommand(
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
