package io.github.flaviodotcom.infrastructure.keycloak;

import io.github.flaviodotcom.domain.identity.CreateIdentityGroupCommand;
import io.github.flaviodotcom.domain.identity.GroupSearchCriteria;
import io.github.flaviodotcom.domain.identity.IdentityGroup;
import io.github.flaviodotcom.domain.identity.IdentityGroupGateway;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.AllArgsConstructor;
import org.keycloak.representations.idm.GroupRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakGroupGateway implements IdentityGroupGateway {

    private static final int FIRST_RESULT = 0;
    private static final int MAX_RESULTS = Integer.MAX_VALUE;

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
                && this.matchesText(criteria.name(), group.name(), criteria.exact())
                && this.matchesAttributes(criteria.attributes(), group.attributes(), criteria.exact());
    }

    private boolean matchesSearch(String filter, IdentityGroup group, boolean exact) {
        if (filter == null) {
            return true;
        }

        return this.matchesText(filter, group.name(), exact)
                || this.matchesText(filter, group.path(), exact);
    }

    private boolean matchesAttributes(
            Map<String, String> requestedAttributes,
            Map<String, List<String>> currentAttributes,
            boolean exact
    ) {
        for (var requestedAttribute : requestedAttributes.entrySet()) {
            var values = currentAttributes.get(requestedAttribute.getKey());
            if (values == null || values.isEmpty()) {
                return false;
            }

            var matched = values.stream().anyMatch(value -> this.matchesText(requestedAttribute.getValue(), value, exact));
            if (!matched) {
                return false;
            }
        }

        return true;
    }

    private boolean matchesText(String filter, String value, boolean exact) {
        if (filter == null) {
            return true;
        }

        if (value == null) {
            return false;
        }

        var normalizedFilter = filter.toLowerCase(Locale.ROOT);
        var normalizedValue = value.toLowerCase(Locale.ROOT);
        return exact
                ? normalizedValue.equals(normalizedFilter)
                : normalizedValue.contains(normalizedFilter);
    }
}
