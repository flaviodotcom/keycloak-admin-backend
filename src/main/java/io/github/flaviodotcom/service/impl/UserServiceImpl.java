package io.github.flaviodotcom.service.impl;

import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.domain.identity.gateway.IdentityCredentialGateway;
import io.github.flaviodotcom.domain.identity.gateway.IdentityUserActionGateway;
import io.github.flaviodotcom.domain.identity.gateway.IdentityMembershipGateway;
import io.github.flaviodotcom.domain.identity.gateway.IdentityRoleAssignmentGateway;
import io.github.flaviodotcom.domain.identity.gateway.IdentitySessionGateway;
import io.github.flaviodotcom.domain.identity.gateway.IdentityUserGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityUser;
import io.github.flaviodotcom.dto.user.CreateUserRequest;
import io.github.flaviodotcom.dto.user.PatchUserRequest;
import io.github.flaviodotcom.dto.user.RequiredActionsRequest;
import io.github.flaviodotcom.dto.user.ResetPasswordRequest;
import io.github.flaviodotcom.dto.user.UpdateUserRequest;
import io.github.flaviodotcom.dto.user.UserGroupResponse;
import io.github.flaviodotcom.dto.user.UserResponse;
import io.github.flaviodotcom.dto.user.UserResponseOptions;
import io.github.flaviodotcom.dto.user.UserSessionResponse;
import io.github.flaviodotcom.dto.pagination.PageRequest;
import io.github.flaviodotcom.dto.pagination.PageResponse;
import io.github.flaviodotcom.service.UserService;
import io.github.flaviodotcom.domain.identity.pagination.IdentitySortComparators;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

import java.util.List;

@ApplicationScoped
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final IdentityUserGateway identityUserGateway;
    private final IdentityMembershipGateway identityMembershipGateway;
    private final IdentityUserActionGateway identityUserActionGateway;
    private final IdentityRoleAssignmentGateway identityRoleAssignmentGateway;
    private final IdentityCredentialGateway identityCredentialGateway;
    private final IdentitySessionGateway identitySessionGateway;

    @Override
    public PageResponse<UserResponse> findUsers(UserSearchCriteria criteria,
                                                UserResponseOptions options,
                                                PageRequest pageRequest) {
        var users = this.identityUserGateway.findUsers(criteria);
        var page = PageResponse.from(users, pageRequest, IdentitySortComparators.userComparator(pageRequest));
        if (!options.includeGroups()) {
            return page.map(UserResponse::fromIdentityUser);
        }

        var groupsByUserId = this.identityMembershipGateway.findUsersGroups(
                page.content().stream()
                        .map(IdentityUser::id)
                        .toList()
        );

        return page.map(user -> UserResponse.fromIdentityUser(
                        user,
                        groupsByUserId.get(user.id()).stream()
                                .map(UserGroupResponse::fromIdentityGroup)
                                .toList()
        ));
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
    public UserResponse patchUser(String id, PatchUserRequest request) {
        return UserResponse.fromIdentityUser(this.identityUserGateway.patchUser(id, request.toCommand()));
    }

    @Override
    public void deleteUser(String id) {
        this.identityUserGateway.deleteUser(id);
    }

    @Override
    public void sendUpdatePasswordEmail(String id) {
        this.identityUserActionGateway.sendUpdatePasswordEmail(id);
    }

    @Override
    public void assignGroup(String id, String groupId) {
        this.identityMembershipGateway.assignUserToGroup(id, groupId);
    }

    @Override
    public void unassignGroup(String id, String groupId) {
        this.identityMembershipGateway.unassignUserFromGroup(id, groupId);
    }

    @Override
    public void assignRealmRole(String id, String roleName) {
        this.identityRoleAssignmentGateway.assignRealmRoleToUser(id, roleName);
    }

    @Override
    public void unassignRealmRole(String id, String roleName) {
        this.identityRoleAssignmentGateway.unassignRealmRoleFromUser(id, roleName);
    }

    @Override
    public void assignClientRole(String id, String clientId, String roleName) {
        this.identityRoleAssignmentGateway.assignClientRoleToUser(id, clientId, roleName);
    }

    @Override
    public void unassignClientRole(String id, String clientId, String roleName) {
        this.identityRoleAssignmentGateway.unassignClientRoleFromUser(id, clientId, roleName);
    }

    @Override
    public void resetPassword(String id, ResetPasswordRequest request) {
        this.identityCredentialGateway.resetPassword(id, request.value(), request.temporary());
    }

    @Override
    public void updateRequiredActions(String id, RequiredActionsRequest request) {
        this.identityCredentialGateway.updateRequiredActions(id, request.actions());
    }

    @Override
    public List<UserSessionResponse> findSessions(String id) {
        return this.identitySessionGateway.findUserSessions(id).stream()
                .map(UserSessionResponse::fromIdentityUserSession)
                .toList();
    }

    @Override
    public void logout(String id) {
        this.identitySessionGateway.logoutUser(id);
    }

    @Override
    public void deleteSession(String id, String sessionId) {
        this.identitySessionGateway.deleteUserSession(id, sessionId);
    }
}
