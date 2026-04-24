package io.github.flaviodotcom.service;

import io.github.flaviodotcom.dto.CreateUserAttributeRequest;
import io.github.flaviodotcom.dto.UserAttributeResponse;

public interface UserAttributeService {

    UserAttributeResponse createAttribute(CreateUserAttributeRequest request);
}
