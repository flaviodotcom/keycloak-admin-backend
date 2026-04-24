package io.github.flaviodotcom.domain.identity.gateway;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityUserCommand;
import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.domain.identity.model.IdentityUser;

import java.util.List;

public interface IdentityUserGateway {

    List<IdentityUser> findUsers(UserSearchCriteria criteria);

    IdentityUser createUser(CreateIdentityUserCommand command);
}
