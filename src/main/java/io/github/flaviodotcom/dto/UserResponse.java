package io.github.flaviodotcom.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.flaviodotcom.domain.identity.model.IdentityUser;

import java.util.List;
import java.util.Map;

public record UserResponse(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        Boolean enabled,
        Boolean emailVerified,
        Long createdTimestamp,
        Map<String, List<String>> attributes,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<UserGroupResponse> groups
) {

    public UserResponse {
        attributes = attributes == null ? Map.of() : attributes;
        groups = groups == null ? List.of() : groups;
    }

    public static UserResponse fromIdentityUser(IdentityUser identityUser) {
        return fromIdentityUser(identityUser, List.of());
    }

    public static UserResponse fromIdentityUser(IdentityUser identityUser, List<UserGroupResponse> groups) {
        return new UserResponse(
                identityUser.id(),
                identityUser.username(),
                identityUser.email(),
                identityUser.firstName(),
                identityUser.lastName(),
                identityUser.enabled(),
                identityUser.emailVerified(),
                identityUser.createdTimestamp(),
                identityUser.attributes(),
                groups
        );
    }
}
