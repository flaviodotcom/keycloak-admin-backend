package io.github.flaviodotcom.domain.identity;

import java.util.List;

public interface IdentityUserGateway {

    List<IdentityUser> findUsers(UserSearchCriteria criteria);

    IdentityUser createUser(CreateIdentityUserCommand command);
}
