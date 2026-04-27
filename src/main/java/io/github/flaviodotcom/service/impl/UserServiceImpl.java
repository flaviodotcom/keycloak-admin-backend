package io.github.flaviodotcom.service.impl;

import io.github.flaviodotcom.domain.identity.gateway.IdentityUserGateway;
import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.dto.CreateUserRequest;
import io.github.flaviodotcom.dto.UpdateUserRequest;
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
    public UserResponse findUserById(String id) {
        return UserResponse.fromIdentityUser(this.identityUserGateway.findUserById(id));
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        return UserResponse.fromIdentityUser(this.identityUserGateway.createUser(request.toCommand()));
    }

    @Override
    public UserResponse updateUser(String id, UpdateUserRequest request) {
        return UserResponse.fromIdentityUser(this.identityUserGateway.updateUser(id, request.toCommand()));
    }

    @Override
    public void deleteUser(String id) {
        this.identityUserGateway.deleteUser(id);
    }
}
