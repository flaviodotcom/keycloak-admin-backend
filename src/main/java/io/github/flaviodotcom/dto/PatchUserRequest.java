package io.github.flaviodotcom.dto;

import io.github.flaviodotcom.domain.identity.command.PatchIdentityUserCommand;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.Map;

public record PatchUserRequest(
        @Pattern(regexp = ".*\\S.*", message = "{validation.username.required}")
        String username,
        String email,
        String firstName,
        String lastName,
        Boolean enabled,
        Boolean emailVerified,
        Map<String, List<String>> attributes
) {

    public PatchIdentityUserCommand toCommand() {
        return new PatchIdentityUserCommand(
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
