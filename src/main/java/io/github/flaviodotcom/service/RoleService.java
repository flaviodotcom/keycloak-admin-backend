package io.github.flaviodotcom.service;

import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.github.flaviodotcom.dto.CreateRoleRequest;
import io.github.flaviodotcom.dto.pagination.PageRequest;
import io.github.flaviodotcom.dto.pagination.PageResponse;
import io.github.flaviodotcom.dto.RoleResponse;
import io.github.flaviodotcom.dto.UpdateRoleRequest;

public interface RoleService {

    PageResponse<RoleResponse> findRoles(RoleSearchCriteria criteria, PageRequest pageRequest);

    RoleResponse findRoleById(String id);

    RoleResponse createRole(CreateRoleRequest request);

    RoleResponse updateRole(String id, UpdateRoleRequest request);

    void deleteRole(String id);
}
