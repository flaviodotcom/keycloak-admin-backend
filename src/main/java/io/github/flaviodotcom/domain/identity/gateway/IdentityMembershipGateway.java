package io.github.flaviodotcom.domain.identity.gateway;

import io.github.flaviodotcom.domain.identity.model.IdentityGroup;
import io.github.flaviodotcom.domain.identity.model.IdentityUser;

import java.util.List;
import java.util.Map;

public interface IdentityMembershipGateway {

    List<IdentityGroup> findUserGroups(String userId);

    void assignUserToGroup(String userId, String groupId);

    void unassignUserFromGroup(String userId, String groupId);

    /**
     * Returns one map entry for every requested user id.
     * Implementations must propagate provider errors instead of omitting failed users.
     */
    Map<String, List<IdentityGroup>> findUsersGroups(List<String> userIds);

    List<IdentityUser> findGroupMembers(String groupId);
}
