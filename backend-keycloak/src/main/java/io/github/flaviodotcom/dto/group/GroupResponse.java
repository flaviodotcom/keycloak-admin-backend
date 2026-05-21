package io.github.flaviodotcom.dto.group;

import io.github.flaviodotcom.domain.identity.model.IdentityGroup;
import io.github.flaviodotcom.infrastructure.interception.contracts.ActionPayload;

import java.util.List;
import java.util.Map;

public record GroupResponse(
        String id,
        String name,
        String path,
        Map<String, List<String>> attributes
) implements ActionPayload {

    public static GroupResponse fromIdentityGroup(IdentityGroup identityGroup) {
        return new GroupResponse(
                identityGroup.id(),
                identityGroup.name(),
                identityGroup.path(),
                identityGroup.attributes()
        );
    }

    @Override
    public String actionSubjectId() {
        return this.id;
    }

    @Override
    public Map<String, Object> actionMetadata() {
        return Map.of(
                "name",
                this.name
        );
    }
}
