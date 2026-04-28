package io.github.flaviodotcom.dto.role;

import io.github.flaviodotcom.domain.identity.command.UpdateIdentityRoleCommand;
import jakarta.validation.constraints.NotBlank;

public record UpdateRoleRequest(
        @NotBlank(message = "{validation.name.required}")
        String name,
        String description
) {

    public UpdateIdentityRoleCommand toCommand() {
        return new UpdateIdentityRoleCommand(this.name, this.description);
    }
}
