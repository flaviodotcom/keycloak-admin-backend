package io.github.flaviodotcom.infrastructure.keycloak.candidate;

import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.domain.shared.SearchTermBuilder;
import io.github.flaviodotcom.domain.shared.TextFilterMatcher;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakQueryDefaults.FIRST_RESULT;
import static io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakQueryDefaults.MAX_RESULTS;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakUserCandidateFinder {

    private final KeycloakAdminSupport keycloak;

    public List<UserRepresentation> findCandidates(UserSearchCriteria criteria) {
        if (criteria.hasAttributeFilters()) {
            return this.keycloak.users().searchByAttributes(this.toAttributeQuery(criteria.attributes()), criteria.exact());
        }

        if (criteria.hasSearchTerm()) {
            return this.searchUsersBySupportedFields(criteria.search(), criteria.exact());
        }

        if (this.hasStructuredTextFilters(criteria)) {
            return this.searchUsersByStructuredTextFilters(criteria);
        }

        if (criteria.enabled() != null) {
            return this.keycloak.users().search(null, criteria.enabled(), FIRST_RESULT, MAX_RESULTS);
        }

        return this.keycloak.users().list(FIRST_RESULT, MAX_RESULTS);
    }

    private List<UserRepresentation> searchUsersBySupportedFields(String search, boolean exact) {
        var usersById = new LinkedHashMap<String, UserRepresentation>();
        for (var term : SearchTermBuilder.build(search)) {
            this.addUsersById(usersById, this.keycloak.users().searchByUsername(term, exact));
            this.addUsersById(usersById, this.keycloak.users().searchByEmail(term, exact));
            this.addUsersById(usersById, this.keycloak.users().searchByFirstName(term, exact));
            this.addUsersById(usersById, this.keycloak.users().searchByLastName(term, exact));
        }
        return List.copyOf(usersById.values());
    }

    private List<UserRepresentation> searchUsersByStructuredTextFilters(UserSearchCriteria criteria) {
        var usersById = new LinkedHashMap<String, UserRepresentation>();
        this.addUsersById(usersById, this.searchUsersBySupportedFields(criteria.username(), criteria.exact()));
        this.addUsersById(usersById, this.searchUsersBySupportedFields(criteria.email(), criteria.exact()));
        this.addUsersById(usersById, this.searchUsersBySupportedFields(criteria.firstName(), criteria.exact()));
        this.addUsersById(usersById, this.searchUsersBySupportedFields(criteria.lastName(), criteria.exact()));
        return List.copyOf(usersById.values());
    }

    private void addUsersById(Map<String, UserRepresentation> usersById, List<UserRepresentation> users) {
        for (var user : users) {
            usersById.putIfAbsent(Objects.requireNonNull(user.getId(), "Keycloak user id is required."), user);
        }
    }

    private String toAttributeQuery(Map<String, String> attributes) {
        return attributes.entrySet().stream()
                .map(attribute -> "%s:%s".formatted(attribute.getKey(), TextFilterMatcher.normalize(attribute.getValue())))
                .collect(Collectors.joining(" "));
    }

    private boolean hasStructuredTextFilters(UserSearchCriteria criteria) {
        return criteria.username() != null
                || criteria.email() != null
                || criteria.firstName() != null
                || criteria.lastName() != null;
    }
}
