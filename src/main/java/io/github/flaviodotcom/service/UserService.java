package io.github.flaviodotcom.service;

import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.dto.CreateUserRequest;
import io.github.flaviodotcom.dto.UpdateUserRequest;
import io.github.flaviodotcom.dto.UserResponse;
import io.github.flaviodotcom.dto.UserResponseOptions;

import java.util.List;

public interface UserService {

    List<UserResponse> findUsers(UserSearchCriteria criteria, UserResponseOptions options);

    UserResponse findUserById(String id, UserResponseOptions options);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(String id, UpdateUserRequest request);

    void deleteUser(String id);
}
