package io.github.flaviodotcom.domain.identity;

import java.util.List;

public interface IdentityRoleGateway {

    List<IdentityRole> findRoles(RoleSearchCriteria criteria);

    IdentityRole createRole(CreateIdentityRoleCommand command);
}
