package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.infrastructure.keycloak.mapper.KeycloakRepresentationMapper;
import io.github.flaviodotcom.infrastructure.keycloak.resilience.KeycloakResilienceExecutor;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KeycloakIdentityMembershipGatewayTest {

    @Test
    void givenUserId_WhenFindUserGroups_ThenReturnGroups() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var usersResource = mock(UsersResource.class);
        var userResource = mock(UserResource.class);
        var gateway = this.gateway(keycloak);

        when(keycloak.users()).thenReturn(usersResource);
        when(usersResource.get("user-1")).thenReturn(userResource);
        when(userResource.groups()).thenReturn(List.of(
                group("group-1", "Financeiro", "/Financeiro"),
                group("group-2", "TI", "/TI")
        ));

        var groups = gateway.findUserGroups("user-1");

        assertEquals("group-1", groups.getFirst().id());
        assertEquals("Financeiro", groups.getFirst().name());
        assertEquals("/Financeiro", groups.getFirst().path());
    }

    @Test
    void givenUserIds_WhenFindUsersGroups_ThenReturnGroupsByUserId() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var usersResource = mock(UsersResource.class);
        var firstUserResource = mock(UserResource.class);
        var secondUserResource = mock(UserResource.class);
        var gateway = this.gateway(keycloak);

        when(keycloak.users()).thenReturn(usersResource);
        when(usersResource.get("user-1")).thenReturn(firstUserResource);
        when(usersResource.get("user-2")).thenReturn(secondUserResource);
        when(firstUserResource.groups()).thenReturn(List.of(group("group-1", "Financeiro", "/Financeiro")));
        when(secondUserResource.groups()).thenReturn(List.of(group("group-2", "TI", "/TI")));

        var groupsByUserId = gateway.findUsersGroups(List.of("user-1", "user-2"));

        assertEquals("group-1", groupsByUserId.get("user-1").getFirst().id());
        assertEquals("group-2", groupsByUserId.get("user-2").getFirst().id());
    }

    @Test
    void givenGroupId_WhenFindGroupMembers_ThenReturnUsers() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var groupsResource = mock(GroupsResource.class);
        var groupResource = mock(GroupResource.class);
        var gateway = this.gateway(keycloak);

        when(keycloak.groups()).thenReturn(groupsResource);
        when(groupsResource.group("group-1")).thenReturn(groupResource);
        when(groupResource.members(0, Integer.MAX_VALUE)).thenReturn(List.of(user("user-1", "john")));

        var members = gateway.findGroupMembers("group-1");

        assertEquals("john", members.getFirst().username());
        verify(groupResource).members(0, Integer.MAX_VALUE);
    }

    @Test
    void givenUserAndGroupIds_WhenAssignUserToGroup_ThenJoinGroup() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var usersResource = mock(UsersResource.class);
        var userResource = mock(UserResource.class);
        var gateway = this.gateway(keycloak);

        when(keycloak.users()).thenReturn(usersResource);
        when(usersResource.get("user-1")).thenReturn(userResource);

        gateway.assignUserToGroup("user-1", "group-1");

        verify(userResource).joinGroup("group-1");
    }

    @Test
    void givenUserAndGroupIds_WhenUnassignUserFromGroup_ThenLeaveGroup() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var usersResource = mock(UsersResource.class);
        var userResource = mock(UserResource.class);
        var gateway = this.gateway(keycloak);

        when(keycloak.users()).thenReturn(usersResource);
        when(usersResource.get("user-1")).thenReturn(userResource);

        gateway.unassignUserFromGroup("user-1", "group-1");

        verify(userResource).leaveGroup("group-1");
    }

    private KeycloakIdentityMembershipGateway gateway(KeycloakAdminSupport keycloak) {
        return new KeycloakIdentityMembershipGateway(
                keycloak,
                new KeycloakRepresentationMapper(),
                new KeycloakResilienceExecutor()
        );
    }

    private GroupRepresentation group(String id, String name, String path) {
        var group = new GroupRepresentation();
        group.setId(id);
        group.setName(name);
        group.setPath(path);
        return group;
    }

    private UserRepresentation user(String id, String username) {
        var user = new UserRepresentation();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
