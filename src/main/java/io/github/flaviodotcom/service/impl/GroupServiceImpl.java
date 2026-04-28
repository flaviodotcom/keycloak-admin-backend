package io.github.flaviodotcom.service.impl;

import io.github.flaviodotcom.domain.identity.criteria.GroupSearchCriteria;
import io.github.flaviodotcom.domain.identity.gateway.IdentityGroupGateway;
import io.github.flaviodotcom.domain.identity.gateway.IdentityMembershipGateway;
import io.github.flaviodotcom.dto.CreateGroupRequest;
import io.github.flaviodotcom.dto.GroupResponse;
import io.github.flaviodotcom.dto.UpdateGroupRequest;
import io.github.flaviodotcom.dto.UserResponse;
import io.github.flaviodotcom.service.GroupService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

import java.util.List;

@ApplicationScoped
@AllArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final IdentityGroupGateway identityGroupGateway;
    private final IdentityMembershipGateway identityMembershipGateway;

    @Override
    public List<GroupResponse> findGroups(GroupSearchCriteria criteria) {
        return this.identityGroupGateway.findGroups(criteria).stream()
                .map(GroupResponse::fromIdentityGroup)
                .toList();
    }

    @Override
    public GroupResponse findGroupById(String id) {
        return GroupResponse.fromIdentityGroup(this.identityGroupGateway.findGroupById(id));
    }

    @Override
    public List<UserResponse> findGroupMembers(String id) {
        return this.identityMembershipGateway.findGroupMembers(id).stream()
                .map(UserResponse::fromIdentityUser)
                .toList();
    }

    @Override
    public GroupResponse createGroup(CreateGroupRequest request) {
        return GroupResponse.fromIdentityGroup(this.identityGroupGateway.createGroup(request.toCommand()));
    }

    @Override
    public GroupResponse updateGroup(String id, UpdateGroupRequest request) {
        return GroupResponse.fromIdentityGroup(this.identityGroupGateway.updateGroup(id, request.toCommand()));
    }

    @Override
    public void deleteGroup(String id) {
        this.identityGroupGateway.deleteGroup(id);
    }
}
