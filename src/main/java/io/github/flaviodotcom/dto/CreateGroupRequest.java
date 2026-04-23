package io.github.flaviodotcom.dto;

import io.github.flaviodotcom.domain.identity.CreateIdentityGroupCommand;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

public record CreateGroupRequest(
        @NotBlank(message = "name is required")
        String name,
        String parentGroupId,
        Map<String, List<String>> attributes
) {

    public CreateGroupRequest {
        attributes = attributes == null ? Map.of() : attributes;
    }

    public CreateIdentityGroupCommand toCommand() {
        return new CreateIdentityGroupCommand(this.name, this.parentGroupId, this.attributes);
    }
}
