package io.github.flaviodotcom.domain.identity.gateway;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityUserCommand;
import io.github.flaviodotcom.domain.identity.command.UpdateIdentityUserCommand;
import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.domain.identity.model.IdentityUser;

import java.util.List;

public interface IdentityUserGateway {

    List<IdentityUser> findUsers(UserSearchCriteria criteria);

    IdentityUser findUserById(String id);

    IdentityUser createUser(CreateIdentityUserCommand command);

    IdentityUser updateUser(String id, UpdateIdentityUserCommand command);

    void deleteUser(String id);
}
