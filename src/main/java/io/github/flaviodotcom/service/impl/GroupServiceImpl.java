package io.github.flaviodotcom.service.impl;

import io.github.flaviodotcom.domain.identity.GroupSearchCriteria;
import io.github.flaviodotcom.domain.identity.IdentityGroupGateway;
import io.github.flaviodotcom.dto.CreateGroupRequest;
import io.github.flaviodotcom.dto.GroupResponse;
import io.github.flaviodotcom.service.GroupService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

import java.util.List;

@ApplicationScoped
@AllArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final IdentityGroupGateway identityGroupGateway;

    @Override
    public List<GroupResponse> findGroups(GroupSearchCriteria criteria) {
        return this.identityGroupGateway.findGroups(criteria).stream()
                .map(GroupResponse::fromIdentityGroup)
                .toList();
    }

    @Override
    public GroupResponse createGroup(CreateGroupRequest request) {
        return GroupResponse.fromIdentityGroup(this.identityGroupGateway.createGroup(request.toCommand()));
    }
}
