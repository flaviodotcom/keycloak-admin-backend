package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityGroupCommand;
import io.github.flaviodotcom.domain.identity.criteria.GroupSearchCriteria;
import io.github.flaviodotcom.domain.identity.gateway.IdentityGroupGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityGroup;
import io.github.flaviodotcom.infrastructure.keycloak.mapper.KeycloakRepresentationMapper;
import io.github.flaviodotcom.infrastructure.keycloak.support.CreatedResourceLocation;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakFilterMatcher;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakHttpResponseHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.AllArgsConstructor;
import org.keycloak.representations.idm.GroupRepresentation;

import java.util.ArrayList;
import java.util.List;

import static io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakQueryDefaults.FIRST_RESULT;
import static io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakQueryDefaults.MAX_RESULTS;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakGroupGateway implements IdentityGroupGateway {

    private final KeycloakAdminSupport keycloak;
    private final KeycloakRepresentationMapper mapper;

    @Override
    public List<IdentityGroup> findGroups(GroupSearchCriteria criteria) {
        try {
            return this.findAllGroups().stream()
                    .map(this.mapper::toIdentityGroup)
                    .filter(group -> this.matches(group, criteria))
                    .toList();
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    @Override
    public IdentityGroup createGroup(CreateIdentityGroupCommand command) {
        try {
            var groupRepresentation = this.mapper.toGroupRepresentation(command);

            try (var response = command.parentGroupId() == null
                    ? this.keycloak.groups().add(groupRepresentation)
                    : this.keycloak.groups().group(command.parentGroupId()).subGroup(groupRepresentation)) {
                KeycloakHttpResponseHandler.ensureCreated(response);
                var groupId = CreatedResourceLocation.extractId(response);
                return this.mapper.toIdentityGroup(this.keycloak.groups().group(groupId).toRepresentation());
            }
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    private List<GroupRepresentation> findAllGroups() {
        var rootGroups = this.keycloak.groups().groups(null, FIRST_RESULT, MAX_RESULTS, false);
        return this.flatten(rootGroups);
    }

    private List<GroupRepresentation> flatten(List<GroupRepresentation> groups) {
        var flattened = new ArrayList<GroupRepresentation>();
        for (var group : groups) {
            flattened.add(group);
            flattened.addAll(this.flatten(group.getSubGroups() == null ? List.of() : group.getSubGroups()));
        }
        return flattened;
    }

    private boolean matches(IdentityGroup group, GroupSearchCriteria criteria) {
        return this.matchesSearch(criteria.search(), group, criteria.exact())
                && KeycloakFilterMatcher.matchesText(criteria.name(), group.name(), criteria.exact())
                && KeycloakFilterMatcher.matchesAttributes(criteria.attributes(), group.attributes(), criteria.exact());
    }

    private boolean matchesSearch(String filter, IdentityGroup group, boolean exact) {
        if (filter == null) {
            return true;
        }

        return KeycloakFilterMatcher.matchesText(filter, group.name(), exact)
                || KeycloakFilterMatcher.matchesText(filter, group.path(), exact);
    }
}
