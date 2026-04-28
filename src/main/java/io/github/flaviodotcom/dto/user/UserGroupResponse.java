package io.github.flaviodotcom.dto.user;

import io.github.flaviodotcom.domain.identity.model.IdentityGroup;

public record UserGroupResponse(
        String id,
        String name,
        String path
) {

    public static UserGroupResponse fromIdentityGroup(IdentityGroup group) {
        return new UserGroupResponse(group.id(), group.name(), group.path());
    }
}
