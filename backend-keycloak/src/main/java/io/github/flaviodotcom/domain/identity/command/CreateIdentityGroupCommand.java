package io.github.flaviodotcom.domain.identity.command;

import java.util.List;
import java.util.Map;

public record CreateIdentityGroupCommand(
        String name,
        String parentGroupId,
        Map<String, List<String>> attributes
) {

    public CreateIdentityGroupCommand {
        attributes = attributes == null ? Map.of() : attributes;
    }
}
