package io.github.flaviodotcom.domain.identity.gateway;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityGroupCommand;
import io.github.flaviodotcom.domain.identity.criteria.GroupSearchCriteria;
import io.github.flaviodotcom.domain.identity.model.IdentityGroup;

import java.util.List;

public interface IdentityGroupGateway {

    List<IdentityGroup> findGroups(GroupSearchCriteria criteria);

    IdentityGroup createGroup(CreateIdentityGroupCommand command);
}
