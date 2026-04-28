package io.github.flaviodotcom.service;

import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.dto.user.CreateUserRequest;
import io.github.flaviodotcom.dto.user.PatchUserRequest;
import io.github.flaviodotcom.dto.pagination.PageRequest;
import io.github.flaviodotcom.dto.pagination.PageResponse;
import io.github.flaviodotcom.dto.user.UpdateUserRequest;
import io.github.flaviodotcom.dto.user.UserResponse;
import io.github.flaviodotcom.dto.user.UserResponseOptions;

public interface UserService {

    PageResponse<UserResponse> findUsers(UserSearchCriteria criteria, UserResponseOptions options, PageRequest pageRequest);

    UserResponse findUserById(String id, UserResponseOptions options);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(String id, UpdateUserRequest request);

    UserResponse patchUser(String id, PatchUserRequest request);

    void deleteUser(String id);

    void sendUpdatePasswordEmail(String id);
}
