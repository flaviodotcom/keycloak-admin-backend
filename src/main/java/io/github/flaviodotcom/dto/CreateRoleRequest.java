package io.github.flaviodotcom.dto;

import io.github.flaviodotcom.domain.identity.CreateIdentityRoleCommand;
import jakarta.validation.constraints.NotBlank;

public record CreateRoleRequest(
        @NotBlank(message = "name is required")
        String name,
        String description
) {

    public CreateIdentityRoleCommand toCommand() {
        return new CreateIdentityRoleCommand(this.name, this.description);
    }
}
