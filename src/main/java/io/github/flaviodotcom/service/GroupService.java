package io.github.flaviodotcom.service;

import io.github.flaviodotcom.domain.identity.criteria.GroupSearchCriteria;
import io.github.flaviodotcom.dto.group.CreateGroupRequest;
import io.github.flaviodotcom.dto.group.GroupResponse;
import io.github.flaviodotcom.dto.pagination.PageRequest;
import io.github.flaviodotcom.dto.pagination.PageResponse;
import io.github.flaviodotcom.dto.group.UpdateGroupRequest;
import io.github.flaviodotcom.dto.user.UserResponse;

public interface GroupService {

    PageResponse<GroupResponse> findGroups(GroupSearchCriteria criteria, PageRequest pageRequest);

    GroupResponse findGroupById(String id);

    PageResponse<UserResponse> findGroupMembers(String id, PageRequest pageRequest);

    GroupResponse createGroup(CreateGroupRequest request);

    GroupResponse updateGroup(String id, UpdateGroupRequest request);

    void deleteGroup(String id);

    void assignRealmRole(String id, String roleName);

    void unassignRealmRole(String id, String roleName);

    void assignClientRole(String id, String clientId, String roleName);

    void unassignClientRole(String id, String clientId, String roleName);
}
