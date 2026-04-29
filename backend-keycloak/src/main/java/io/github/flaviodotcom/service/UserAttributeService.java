package io.github.flaviodotcom.service;

import io.github.flaviodotcom.dto.userattribute.CreateUserAttributeRequest;
import io.github.flaviodotcom.dto.userattribute.UpdateUserAttributeRequest;
import io.github.flaviodotcom.dto.userattribute.UserAttributeResponse;

public interface UserAttributeService {

    UserAttributeResponse createAttribute(CreateUserAttributeRequest request);

    UserAttributeResponse updateAttribute(String name, UpdateUserAttributeRequest request);

    void deleteAttribute(String name);
}
