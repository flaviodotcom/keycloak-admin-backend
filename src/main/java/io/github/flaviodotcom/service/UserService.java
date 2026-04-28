package io.github.flaviodotcom.service;

import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.dto.CreateUserRequest;
import io.github.flaviodotcom.dto.pagination.PageRequest;
import io.github.flaviodotcom.dto.pagination.PageResponse;
import io.github.flaviodotcom.dto.UpdateUserRequest;
import io.github.flaviodotcom.dto.UserResponse;
import io.github.flaviodotcom.dto.UserResponseOptions;

public interface UserService {

    PageResponse<UserResponse> findUsers(UserSearchCriteria criteria, UserResponseOptions options, PageRequest pageRequest);

    UserResponse findUserById(String id, UserResponseOptions options);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(String id, UpdateUserRequest request);

    void deleteUser(String id);
}
