package io.github.flaviodotcom.service.impl;

import io.github.flaviodotcom.domain.identity.IdentityUserGateway;
import io.github.flaviodotcom.domain.identity.UserSearchCriteria;
import io.github.flaviodotcom.dto.CreateUserRequest;
import io.github.flaviodotcom.dto.UserResponse;
import io.github.flaviodotcom.service.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

import java.util.List;

@ApplicationScoped
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final IdentityUserGateway identityUserGateway;

    @Override
    public List<UserResponse> findUsers(UserSearchCriteria criteria) {
        return this.identityUserGateway.findUsers(criteria).stream()
                .map(UserResponse::fromIdentityUser)
                .toList();
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        return UserResponse.fromIdentityUser(this.identityUserGateway.createUser(request.toCommand()));
    }
}
