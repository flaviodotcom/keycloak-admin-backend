package io.github.flaviodotcom.domain.identity.command;

import java.util.List;
import java.util.Map;

public record UpdateIdentityGroupCommand(
        String name,
        Map<String, List<String>> attributes
) {

    public UpdateIdentityGroupCommand {
        attributes = attributes == null ? Map.of() : attributes;
    }
}
