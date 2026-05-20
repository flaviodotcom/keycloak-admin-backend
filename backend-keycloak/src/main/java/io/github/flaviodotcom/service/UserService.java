package io.github.flaviodotcom.service;

import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.dto.user.CreateUserRequest;
import io.github.flaviodotcom.dto.user.PatchUserRequest;
import io.github.flaviodotcom.dto.pagination.PageRequest;
import io.github.flaviodotcom.dto.pagination.PageResponse;
import io.github.flaviodotcom.dto.user.RequiredActionsRequest;
import io.github.flaviodotcom.dto.user.ResetPasswordRequest;
import io.github.flaviodotcom.dto.user.UpdateUserRequest;
import io.github.flaviodotcom.dto.user.UserResponse;
import io.github.flaviodotcom.dto.user.UserResponseOptions;
import io.github.flaviodotcom.dto.user.UserSessionResponse;
import io.github.flaviodotcom.infrastructure.interception.contracts.DeletedSubjectPayload;

import java.util.List;

public interface UserService {

    PageResponse<UserResponse> findUsers(UserSearchCriteria criteria, UserResponseOptions options, PageRequest pageRequest);

    UserResponse findUserById(String id, UserResponseOptions options);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(String id, UpdateUserRequest request);

    UserResponse patchUser(String id, PatchUserRequest request);

    DeletedSubjectPayload deleteUser(String id);

    void sendUpdatePasswordEmail(String id);

    void assignGroup(String id, String groupId);

    void unassignGroup(String id, String groupId);

    void assignRealmRole(String id, String roleName);

    void unassignRealmRole(String id, String roleName);

    void assignClientRole(String id, String clientId, String roleName);

    void unassignClientRole(String id, String clientId, String roleName);

    void resetPassword(String id, ResetPasswordRequest request);

    void updateRequiredActions(String id, RequiredActionsRequest request);

    List<UserSessionResponse> findSessions(String id);

    void logout(String id);

    void deleteSession(String id, String sessionId);
}
