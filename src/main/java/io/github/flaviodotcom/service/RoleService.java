package io.github.flaviodotcom.service;

import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.github.flaviodotcom.dto.CreateRoleRequest;
import io.github.flaviodotcom.dto.RoleResponse;
import io.github.flaviodotcom.dto.UpdateRoleRequest;

import java.util.List;

public interface RoleService {

    List<RoleResponse> findRoles(RoleSearchCriteria criteria);

    RoleResponse findRoleById(String id);

    RoleResponse createRole(CreateRoleRequest request);

    RoleResponse updateRole(String id, UpdateRoleRequest request);

    void deleteRole(String id);
}
