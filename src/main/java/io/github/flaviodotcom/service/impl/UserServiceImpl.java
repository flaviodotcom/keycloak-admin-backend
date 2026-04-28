package io.github.flaviodotcom.service.impl;

import io.github.flaviodotcom.domain.identity.gateway.IdentityUserGateway;
import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.domain.identity.gateway.IdentityMembershipGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityUser;
import io.github.flaviodotcom.dto.CreateUserRequest;
import io.github.flaviodotcom.dto.UpdateUserRequest;
import io.github.flaviodotcom.dto.UserGroupResponse;
import io.github.flaviodotcom.dto.UserResponse;
import io.github.flaviodotcom.dto.UserResponseOptions;
import io.github.flaviodotcom.service.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

import java.util.List;

@ApplicationScoped
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final IdentityUserGateway identityUserGateway;
    private final IdentityMembershipGateway identityMembershipGateway;

    @Override
    public List<UserResponse> findUsers(UserSearchCriteria criteria, UserResponseOptions options) {
        var users = this.identityUserGateway.findUsers(criteria);
        if (!options.includeGroups()) {
            return users.stream()
                    .map(UserResponse::fromIdentityUser)
                    .toList();
        }

        var groupsByUserId = this.identityMembershipGateway.findUsersGroups(
                users.stream()
                        .map(IdentityUser::id)
                        .toList()
        );

        return users.stream()
                .map(user -> UserResponse.fromIdentityUser(
                        user,
                        groupsByUserId.get(user.id()).stream()
                                .map(UserGroupResponse::fromIdentityGroup)
                                .toList()
                ))
                .toList();
    }

    @Override
    public UserResponse findUserById(String id, UserResponseOptions options) {
        var user = this.identityUserGateway.findUserById(id);
        if (!options.includeGroups()) {
            return UserResponse.fromIdentityUser(user);
        }

        return UserResponse.fromIdentityUser(
                user,
                this.identityMembershipGateway.findUserGroups(id).stream()
                        .map(UserGroupResponse::fromIdentityGroup)
                        .toList()
        );
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
