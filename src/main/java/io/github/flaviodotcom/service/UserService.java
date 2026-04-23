package io.github.flaviodotcom.service;

import io.github.flaviodotcom.domain.identity.UserSearchCriteria;
import io.github.flaviodotcom.dto.CreateUserRequest;
import io.github.flaviodotcom.dto.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> findUsers(UserSearchCriteria criteria);

    UserResponse createUser(CreateUserRequest request);
}
