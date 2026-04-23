package io.github.flaviodotcom.domain.identity;

import java.util.List;
import java.util.Map;

public record IdentityGroup(
        String id,
        String name,
        String path,
        Map<String, List<String>> attributes
) {

    public IdentityGroup {
        attributes = attributes == null ? Map.of() : attributes;
    }
}
