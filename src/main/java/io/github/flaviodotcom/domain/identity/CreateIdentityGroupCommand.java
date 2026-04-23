package io.github.flaviodotcom.domain.identity;

import java.util.Map;

public record CreateIdentityGroupCommand(
        String name,
        String parentGroupId,
        Map<String, java.util.List<String>> attributes
) {

    public CreateIdentityGroupCommand {
        attributes = attributes == null ? Map.of() : attributes;
    }
}
