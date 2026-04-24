package io.github.flaviodotcom.service.impl;

import io.github.flaviodotcom.domain.identity.gateway.IdentityRoleGateway;
import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.github.flaviodotcom.dto.CreateRoleRequest;
import io.github.flaviodotcom.dto.RoleResponse;
import io.github.flaviodotcom.service.RoleService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

import java.util.List;

@ApplicationScoped
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final IdentityRoleGateway identityRoleGateway;

    @Override
    public List<RoleResponse> findRoles(RoleSearchCriteria criteria) {
        return this.identityRoleGateway.findRoles(criteria).stream()
                .map(RoleResponse::fromIdentityRole)
                .toList();
    }

    @Override
    public RoleResponse createRole(CreateRoleRequest request) {
        return RoleResponse.fromIdentityRole(this.identityRoleGateway.createRole(request.toCommand()));
    }
}
