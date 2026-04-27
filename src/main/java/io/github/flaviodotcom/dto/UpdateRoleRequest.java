package io.github.flaviodotcom.dto;

import io.github.flaviodotcom.domain.identity.command.UpdateIdentityRoleCommand;
import jakarta.validation.constraints.NotBlank;

public record UpdateRoleRequest(
        @NotBlank(message = "name is required")
        String name,
        String description
) {

    public UpdateIdentityRoleCommand toCommand() {
        return new UpdateIdentityRoleCommand(this.name, this.description);
    }
}
