package io.github.flaviodotcom.domain.identity.gateway;

public interface IdentityRoleAssignmentGateway {

    void assignRealmRoleToUser(String userId, String roleName);

    void unassignRealmRoleFromUser(String userId, String roleName);

    void assignClientRoleToUser(String userId, String clientId, String roleName);

    void unassignClientRoleFromUser(String userId, String clientId, String roleName);

    void assignRealmRoleToGroup(String groupId, String roleName);

    void unassignRealmRoleFromGroup(String groupId, String roleName);

    void assignClientRoleToGroup(String groupId, String clientId, String roleName);

    void unassignClientRoleFromGroup(String groupId, String clientId, String roleName);
}
