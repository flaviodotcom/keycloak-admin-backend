package io.github.flaviodotcom.service;

import io.github.flaviodotcom.domain.identity.criteria.GroupSearchCriteria;
import io.github.flaviodotcom.dto.CreateGroupRequest;
import io.github.flaviodotcom.dto.GroupResponse;
import io.github.flaviodotcom.dto.UpdateGroupRequest;
import io.github.flaviodotcom.dto.UserResponse;

import java.util.List;

public interface GroupService {

    List<GroupResponse> findGroups(GroupSearchCriteria criteria);

    GroupResponse findGroupById(String id);

    List<UserResponse> findGroupMembers(String id);

    GroupResponse createGroup(CreateGroupRequest request);

    GroupResponse updateGroup(String id, UpdateGroupRequest request);

    void deleteGroup(String id);
}
