package io.github.flaviodotcom.domain.identity;

import java.util.List;

public interface IdentityGroupGateway {

    List<IdentityGroup> findGroups(GroupSearchCriteria criteria);

    IdentityGroup createGroup(CreateIdentityGroupCommand command);
}
