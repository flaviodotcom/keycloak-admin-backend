package io.github.flaviodotcom.domain.identity.gateway;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityUserAttributeCommand;
import io.github.flaviodotcom.domain.identity.model.IdentityUserAttribute;

import java.util.Map;
import java.util.Set;

public interface IdentityUserAttributeGateway {

    IdentityUserAttribute createAttribute(CreateIdentityUserAttributeCommand command);

    IdentityUserAttribute findAttribute(String name);

    Map<String, IdentityUserAttribute> findAttributes(Set<String> names);
}
