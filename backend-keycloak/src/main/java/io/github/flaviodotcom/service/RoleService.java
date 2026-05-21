package io.github.flaviodotcom.service;

import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.github.flaviodotcom.dto.role.CreateRoleRequest;
import io.github.flaviodotcom.dto.pagination.PageRequest;
import io.github.flaviodotcom.dto.pagination.PageResponse;
import io.github.flaviodotcom.dto.role.RoleResponse;
import io.github.flaviodotcom.dto.role.UpdateRoleRequest;
import io.github.flaviodotcom.infrastructure.interception.contracts.DeletedSubjectPayload;

public interface RoleService {

    PageResponse<RoleResponse> findRoles(RoleSearchCriteria criteria, PageRequest pageRequest);

    RoleResponse findRoleById(String id);

    RoleResponse createRole(CreateRoleRequest request);

    RoleResponse updateRole(String id, UpdateRoleRequest request);

    DeletedSubjectPayload deleteRole(String id);
}
