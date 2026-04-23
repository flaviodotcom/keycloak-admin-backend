package io.github.flaviodotcom.dto;

import io.github.flaviodotcom.domain.identity.IdentityUser;

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
        Map<String, List<String>> attributes
) {

    public static UserResponse fromIdentityUser(IdentityUser identityUser) {
        return new UserResponse(
                identityUser.id(),
                identityUser.username(),
                identityUser.email(),
                identityUser.firstName(),
                identityUser.lastName(),
                identityUser.enabled(),
                identityUser.emailVerified(),
                identityUser.createdTimestamp(),
                identityUser.attributes()
        );
    }
}
