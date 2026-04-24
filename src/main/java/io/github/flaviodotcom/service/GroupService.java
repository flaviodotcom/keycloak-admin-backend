package io.github.flaviodotcom.service;

import io.github.flaviodotcom.domain.identity.criteria.GroupSearchCriteria;
import io.github.flaviodotcom.dto.CreateGroupRequest;
import io.github.flaviodotcom.dto.GroupResponse;

import java.util.List;

public interface GroupService {

    List<GroupResponse> findGroups(GroupSearchCriteria criteria);

    GroupResponse createGroup(CreateGroupRequest request);
}
