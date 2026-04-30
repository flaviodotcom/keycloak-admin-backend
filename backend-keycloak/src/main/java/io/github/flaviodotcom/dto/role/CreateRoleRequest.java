package io.github.flaviodotcom.dto.role;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityRoleCommand;
import jakarta.validation.constraints.NotBlank;

public record CreateRoleRequest(
        @NotBlank(message = "{validation.name.required}")
        String name,
        String description
) {

    public CreateIdentityRoleCommand toCommand() {
        return new CreateIdentityRoleCommand(this.name, this.description);
    }
}
