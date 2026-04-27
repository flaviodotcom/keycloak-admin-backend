package io.github.flaviodotcom.dto;

import io.github.flaviodotcom.domain.identity.command.UpdateIdentityGroupCommand;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

public record UpdateGroupRequest(
        @NotBlank(message = "name is required")
        String name,
        Map<String, List<String>> attributes
) {

    public UpdateGroupRequest {
        attributes = attributes == null ? Map.of() : attributes;
    }

    public UpdateIdentityGroupCommand toCommand() {
        return new UpdateIdentityGroupCommand(this.name, this.attributes);
    }
}
