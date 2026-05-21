package io.github.flaviodotcom.service.impl;

import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.github.flaviodotcom.domain.identity.gateway.IdentityRoleGateway;
import io.github.flaviodotcom.domain.identity.pagination.IdentitySortComparators;
import io.github.flaviodotcom.dto.pagination.PageRequest;
import io.github.flaviodotcom.dto.pagination.PageResponse;
import io.github.flaviodotcom.dto.role.CreateRoleRequest;
import io.github.flaviodotcom.dto.role.RoleResponse;
import io.github.flaviodotcom.dto.role.UpdateRoleRequest;
import io.github.flaviodotcom.infrastructure.interception.contracts.DeletedSubjectPayload;
import io.github.flaviodotcom.infrastructure.interception.identityevent.PublishIdentityEvent;
import io.github.flaviodotcom.service.RoleService;
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
    @PublishIdentityEvent(
            eventType = "identity.role.created",
            subjectType = "role"
    )
    public RoleResponse createRole(CreateRoleRequest request) {
        var role = this.identityRoleGateway.createRole(request.toCommand());
        return RoleResponse.fromIdentityRole(role);
    }

    @Override
    @PublishIdentityEvent(
            eventType = "identity.role.updated",
            subjectType = "role"
    )
    public RoleResponse updateRole(String id, UpdateRoleRequest request) {
        var role = this.identityRoleGateway.updateRole(id, request.toCommand());
        return RoleResponse.fromIdentityRole(role);
    }

    @Override
    @PublishIdentityEvent(
            eventType = "identity.role.deleted",
            subjectType = "role"
    )
    public DeletedSubjectPayload deleteRole(String id) {
        this.identityRoleGateway.deleteRole(id);
        return new DeletedSubjectPayload(id);
    }
}
