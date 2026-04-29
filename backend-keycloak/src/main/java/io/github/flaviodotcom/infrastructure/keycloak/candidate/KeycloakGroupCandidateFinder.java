package io.github.flaviodotcom.infrastructure.keycloak.candidate;

import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import org.keycloak.representations.idm.GroupRepresentation;

import java.util.ArrayList;
import java.util.List;

import static io.github.flaviodotcom.infrastructure.keycloak.pagination.KeycloakQueryDefaults.FIRST_RESULT;
import static io.github.flaviodotcom.infrastructure.keycloak.pagination.KeycloakQueryDefaults.MAX_RESULTS;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakGroupCandidateFinder {

    private final KeycloakAdminSupport keycloak;

    public List<GroupRepresentation> findCandidates() {
        var rootGroups = this.keycloak.groups().groups(null, FIRST_RESULT, MAX_RESULTS, false);
        return this.findNestedGroups(rootGroups);
    }

    private List<GroupRepresentation> findNestedGroups(List<GroupRepresentation> groups) {
        var nestedGroups = new ArrayList<GroupRepresentation>();
        for (var group : groups) {
            var groupResource = this.keycloak.groups().group(group.getId());
            nestedGroups.add(groupResource.toRepresentation());
            var subGroups = groupResource.getSubGroups(FIRST_RESULT, MAX_RESULTS, false);
            nestedGroups.addAll(this.findNestedGroups(subGroups));
        }
        return nestedGroups;
    }
}
