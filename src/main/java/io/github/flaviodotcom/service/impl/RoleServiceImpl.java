package io.github.flaviodotcom.service.impl;

import io.github.flaviodotcom.domain.identity.gateway.IdentityRoleGateway;
import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.github.flaviodotcom.dto.CreateRoleRequest;
import io.github.flaviodotcom.dto.pagination.PageRequest;
import io.github.flaviodotcom.dto.pagination.PageResponse;
import io.github.flaviodotcom.dto.RoleResponse;
import io.github.flaviodotcom.dto.UpdateRoleRequest;
import io.github.flaviodotcom.service.RoleService;
import io.github.flaviodotcom.service.pagination.IdentitySortComparators;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final IdentityRoleGateway identityRoleGateway;

    @Override
    public PageResponse<RoleResponse> findRoles(RoleSearchCriteria criteria, PageRequest pageRequest) {
        return PageResponse.from(
                this.identityRoleGateway.findRoles(criteria),
                pageRequest,
                IdentitySortComparators.roleComparator(pageRequest)
        ).map(RoleResponse::fromIdentityRole);
    }

    @Override
    public RoleResponse findRoleById(String id) {
        return RoleResponse.fromIdentityRole(this.identityRoleGateway.findRoleById(id));
    }

    @Override
    public RoleResponse createRole(CreateRoleRequest request) {
        return RoleResponse.fromIdentityRole(this.identityRoleGateway.createRole(request.toCommand()));
    }

    @Override
    public RoleResponse updateRole(String id, UpdateRoleRequest request) {
        return RoleResponse.fromIdentityRole(this.identityRoleGateway.updateRole(id, request.toCommand()));
    }

    @Override
    public void deleteRole(String id) {
        this.identityRoleGateway.deleteRole(id);
    }
}
