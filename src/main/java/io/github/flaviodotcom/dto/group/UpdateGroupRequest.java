package io.github.flaviodotcom.dto.group;

import io.github.flaviodotcom.config.validators.ValidAttributes;
import io.github.flaviodotcom.domain.identity.command.UpdateIdentityGroupCommand;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

public record UpdateGroupRequest(
        @NotBlank(message = "{validation.name.required}")
        String name,
        @ValidAttributes
        Map<String, List<String>> attributes
) {

    public UpdateGroupRequest {
        attributes = attributes == null ? Map.of() : attributes;
    }

    public UpdateIdentityGroupCommand toCommand() {
        return new UpdateIdentityGroupCommand(this.name, this.attributes);
    }
}
