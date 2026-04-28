package io.github.flaviodotcom.service.impl;

import io.github.flaviodotcom.domain.identity.gateway.IdentityUserAttributeGateway;
import io.github.flaviodotcom.dto.userattribute.CreateUserAttributeRequest;
import io.github.flaviodotcom.dto.userattribute.UserAttributeResponse;
import io.github.flaviodotcom.service.UserAttributeService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class UserAttributeServiceImpl implements UserAttributeService {

    private final IdentityUserAttributeGateway identityUserAttributeGateway;

    @Override
    public UserAttributeResponse createAttribute(CreateUserAttributeRequest request) {
        return UserAttributeResponse.fromIdentityUserAttribute(
                this.identityUserAttributeGateway.createAttribute(request.toCommand())
        );
    }
}
