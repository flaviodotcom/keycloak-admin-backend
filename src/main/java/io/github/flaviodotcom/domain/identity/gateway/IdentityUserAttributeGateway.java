package io.github.flaviodotcom.domain.identity.gateway;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityUserAttributeCommand;
import io.github.flaviodotcom.domain.identity.model.IdentityUserAttribute;

public interface IdentityUserAttributeGateway {

    IdentityUserAttribute createAttribute(CreateIdentityUserAttributeCommand command);

    IdentityUserAttribute findAttribute(String name);
}
