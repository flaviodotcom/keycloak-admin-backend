package io.github.flaviodotcom.service.impl;

import io.github.flaviodotcom.domain.identity.gateway.IdentityRoleGateway;
import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.github.flaviodotcom.dto.role.CreateRoleRequest;
import io.github.flaviodotcom.dto.pagination.PageRequest;
import io.github.flaviodotcom.dto.pagination.PageResponse;
import io.github.flaviodotcom.dto.role.RoleResponse;
import io.github.flaviodotcom.dto.role.UpdateRoleRequest;
import io.github.flaviodotcom.service.RoleService;
import io.github.flaviodotcom.domain.identity.pagination.IdentitySortComparators;
import io.github.flaviodotcom.service.events.IdentityEventPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

import java.util.Map;

@ApplicationScoped
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final IdentityRoleGateway identityRoleGateway;
    private final IdentityEventPublisher identityEventPublisher;

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
        var role = this.identityRoleGateway.createRole(request.toCommand());
        this.identityEventPublisher.publish(
                "identity.role.created",
                "role",
                role.id(),
                Map.of("name", role.name())
        );
        return RoleResponse.fromIdentityRole(role);
    }

    @Override
    public RoleResponse updateRole(String id, UpdateRoleRequest request) {
        var role = this.identityRoleGateway.updateRole(id, request.toCommand());
        this.identityEventPublisher.publish(
                "identity.role.updated",
                "role",
                role.id(),
                Map.of("name", role.name())
        );
        return RoleResponse.fromIdentityRole(role);
    }

    @Override
    public void deleteRole(String id) {
        this.identityRoleGateway.deleteRole(id);
        this.identityEventPublisher.publish("identity.role.deleted", "role", id, Map.of());
    }
}
