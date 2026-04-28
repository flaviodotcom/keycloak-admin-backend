package io.github.flaviodotcom.dto;

import io.github.flaviodotcom.config.validators.ValidAttributes;
import io.github.flaviodotcom.domain.identity.command.CreateIdentityGroupCommand;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

public record CreateGroupRequest(
        @NotBlank(message = "{validation.name.required}")
        String name,
        String parentGroupId,
        @ValidAttributes
        Map<String, List<String>> attributes
) {

    public CreateGroupRequest {
        attributes = attributes == null ? Map.of() : attributes;
    }

    public CreateIdentityGroupCommand toCommand() {
        return new CreateIdentityGroupCommand(this.name, this.parentGroupId, this.attributes);
    }
}
