package io.github.flaviodotcom.service;

import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.github.flaviodotcom.dto.CreateRoleRequest;
import io.github.flaviodotcom.dto.RoleResponse;

import java.util.List;

public interface RoleService {

    List<RoleResponse> findRoles(RoleSearchCriteria criteria);

    RoleResponse createRole(CreateRoleRequest request);
}
