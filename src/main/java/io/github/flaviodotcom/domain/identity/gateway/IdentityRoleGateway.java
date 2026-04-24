package io.github.flaviodotcom.domain.identity.gateway;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityRoleCommand;
import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.github.flaviodotcom.domain.identity.model.IdentityRole;

import java.util.List;

public interface IdentityRoleGateway {

    List<IdentityRole> findRoles(RoleSearchCriteria criteria);

    IdentityRole createRole(CreateIdentityRoleCommand command);
}
