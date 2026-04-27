package io.github.flaviodotcom.domain.identity.gateway;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityGroupCommand;
import io.github.flaviodotcom.domain.identity.command.UpdateIdentityGroupCommand;
import io.github.flaviodotcom.domain.identity.criteria.GroupSearchCriteria;
import io.github.flaviodotcom.domain.identity.model.IdentityGroup;

import java.util.List;

public interface IdentityGroupGateway {

    List<IdentityGroup> findGroups(GroupSearchCriteria criteria);

    IdentityGroup findGroupById(String id);

    IdentityGroup createGroup(CreateIdentityGroupCommand command);

    IdentityGroup updateGroup(String id, UpdateIdentityGroupCommand command);

    void deleteGroup(String id);
}
