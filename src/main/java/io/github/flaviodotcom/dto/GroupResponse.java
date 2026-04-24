package io.github.flaviodotcom.dto;

import io.github.flaviodotcom.domain.identity.model.IdentityGroup;

import java.util.List;
import java.util.Map;

public record GroupResponse(
        String id,
        String name,
        String path,
        Map<String, List<String>> attributes
) {

    public static GroupResponse fromIdentityGroup(IdentityGroup identityGroup) {
        return new GroupResponse(
                identityGroup.id(),
                identityGroup.name(),
                identityGroup.path(),
                identityGroup.attributes()
        );
    }
}
