package io.github.flaviodotcom.service.impl;

import io.github.flaviodotcom.domain.identity.criteria.GroupSearchCriteria;
import io.github.flaviodotcom.domain.identity.gateway.IdentityGroupGateway;
import io.github.flaviodotcom.domain.identity.gateway.IdentityMembershipGateway;
import io.github.flaviodotcom.domain.identity.gateway.IdentityRoleAssignmentGateway;
import io.github.flaviodotcom.dto.group.CreateGroupRequest;
import io.github.flaviodotcom.dto.group.GroupResponse;
import io.github.flaviodotcom.dto.pagination.PageRequest;
import io.github.flaviodotcom.dto.pagination.PageResponse;
import io.github.flaviodotcom.dto.group.UpdateGroupRequest;
import io.github.flaviodotcom.dto.user.UserResponse;
import io.github.flaviodotcom.service.GroupService;
import io.github.flaviodotcom.domain.identity.pagination.IdentitySortComparators;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final IdentityGroupGateway identityGroupGateway;
    private final IdentityMembershipGateway identityMembershipGateway;
    private final IdentityRoleAssignmentGateway identityRoleAssignmentGateway;

    @Override
    public PageResponse<GroupResponse> findGroups(GroupSearchCriteria criteria, PageRequest pageRequest) {
        return PageResponse.from(
                this.identityGroupGateway.findGroups(criteria),
                pageRequest,
                IdentitySortComparators.groupComparator(pageRequest)
        ).map(GroupResponse::fromIdentityGroup);
    }

    @Override
    public GroupResponse findGroupById(String id) {
        return GroupResponse.fromIdentityGroup(this.identityGroupGateway.findGroupById(id));
    }

    @Override
    public PageResponse<UserResponse> findGroupMembers(String id, PageRequest pageRequest) {
        return PageResponse.from(
                this.identityMembershipGateway.findGroupMembers(id),
                pageRequest,
                IdentitySortComparators.userComparator(pageRequest)
        ).map(UserResponse::fromIdentityUser);
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

    @Override
    public void assignRealmRole(String id, String roleName) {
        this.identityRoleAssignmentGateway.assignRealmRoleToGroup(id, roleName);
    }

    @Override
    public void unassignRealmRole(String id, String roleName) {
        this.identityRoleAssignmentGateway.unassignRealmRoleFromGroup(id, roleName);
    }

    @Override
    public void assignClientRole(String id, String clientId, String roleName) {
        this.identityRoleAssignmentGateway.assignClientRoleToGroup(id, clientId, roleName);
    }

    @Override
    public void unassignClientRole(String id, String clientId, String roleName) {
        this.identityRoleAssignmentGateway.unassignClientRoleFromGroup(id, clientId, roleName);
    }
}
