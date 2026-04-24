package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.criteria.GroupSearchCriteria;
import io.github.flaviodotcom.domain.identity.model.IdentityGroup;
import io.github.flaviodotcom.infrastructure.keycloak.mapper.KeycloakRepresentationMapper;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.representations.idm.GroupRepresentation;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KeycloakGroupGatewayTest {

    @Test
    void givenNestedGroups_WhenFindGroups_ThenReturnRootAndSubGroups() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var groupsResource = mock(GroupsResource.class);
        var parentResource = mock(GroupResource.class);
        var childResource = mock(GroupResource.class);
        var grandchildResource = mock(GroupResource.class);
        var gateway = new KeycloakGroupGateway(keycloak, new KeycloakRepresentationMapper());

        var parent = group("parent-id", "Parent", "/Parent");
        var child = group("child-id", "Child", "/Parent/Child");
        var grandchild = group("grandchild-id", "Grandchild", "/Parent/Child/Grandchild");

        when(keycloak.groups()).thenReturn(groupsResource);
        when(groupsResource.groups(null, 0, Integer.MAX_VALUE, false)).thenReturn(List.of(parent));
        when(groupsResource.group("parent-id")).thenReturn(parentResource);
        when(groupsResource.group("child-id")).thenReturn(childResource);
        when(groupsResource.group("grandchild-id")).thenReturn(grandchildResource);
        when(parentResource.toRepresentation()).thenReturn(parent);
        when(parentResource.getSubGroups(0, Integer.MAX_VALUE, false)).thenReturn(List.of(child));
        when(childResource.toRepresentation()).thenReturn(child);
        when(childResource.getSubGroups(0, Integer.MAX_VALUE, false)).thenReturn(List.of(grandchild));
        when(grandchildResource.toRepresentation()).thenReturn(grandchild);
        when(grandchildResource.getSubGroups(0, Integer.MAX_VALUE, false)).thenReturn(List.of());

        var groups = gateway.findGroups(new GroupSearchCriteria(null, null, false, Map.of()));

        assertEquals(List.of("Parent", "Child", "Grandchild"), groups.stream().map(IdentityGroup::name).toList());
    }

    private GroupRepresentation group(String id, String name, String path) {
        var group = new GroupRepresentation();
        group.setId(id);
        group.setName(name);
        group.setPath(path);
        return group;
    }
}
