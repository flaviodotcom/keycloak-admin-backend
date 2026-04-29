package io.github.flaviodotcom.domain.identity.gateway;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityRoleCommand;
import io.github.flaviodotcom.domain.identity.command.UpdateIdentityRoleCommand;
import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.github.flaviodotcom.domain.identity.model.IdentityRole;

import java.util.List;

public interface IdentityRoleGateway {

    List<IdentityRole> findRoles(RoleSearchCriteria criteria);

    IdentityRole findRoleById(String id);

    IdentityRole createRole(CreateIdentityRoleCommand command);

    IdentityRole updateRole(String id, UpdateIdentityRoleCommand command);

    void deleteRole(String id);
}
