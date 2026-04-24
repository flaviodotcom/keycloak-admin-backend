package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityUserCommand;
import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.domain.identity.gateway.IdentityUserGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityUser;
import io.github.flaviodotcom.infrastructure.keycloak.mapper.KeycloakRepresentationMapper;
import io.github.flaviodotcom.infrastructure.keycloak.support.CreatedResourceLocation;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakFilterMatcher;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakHttpResponseHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
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
public class KeycloakUserGateway implements IdentityUserGateway {

    private final KeycloakAdminSupport keycloak;
    private final KeycloakRepresentationMapper mapper;

    @Override
    public List<IdentityUser> findUsers(UserSearchCriteria criteria) {
        try {
            return this.searchUsers(criteria).stream()
                    .map(this.mapper::toIdentityUser)
                    .filter(user -> this.matches(user, criteria))
                    .toList();
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    @Override
    public IdentityUser createUser(CreateIdentityUserCommand command) {
        try {
            var userRepresentation = this.mapper.toUserRepresentation(command);

            try (var response = this.keycloak.users().create(userRepresentation)) {
                KeycloakHttpResponseHandler.ensureCreated(response);
                var userId = CreatedResourceLocation.extractId(response);
                return this.mapper.toIdentityUser(this.keycloak.users().get(userId).toRepresentation());
            }
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    private List<UserRepresentation> searchUsers(UserSearchCriteria criteria) {
        if (criteria.hasAttributeFilters()) {
            return this.keycloak.users().searchByAttributes(this.toAttributeQuery(criteria.attributes()), criteria.exact());
        }

        if (criteria.hasSearchTerm()) {
            return this.searchUsersBySupportedFields(criteria.search(), criteria.exact());
        }

        if (criteria.hasStructuredFilters()) {
            return this.keycloak.users().search(
                    criteria.username(),
                    criteria.firstName(),
                    criteria.lastName(),
                    criteria.email(),
                    FIRST_RESULT,
                    MAX_RESULTS,
                    criteria.enabled(),
                    false,
                    criteria.exact()
            );
        }

        return this.keycloak.users().list(FIRST_RESULT, MAX_RESULTS);
    }

    private List<UserRepresentation> searchUsersBySupportedFields(String search, boolean exact) {
        var usersById = new LinkedHashMap<String, UserRepresentation>();
        this.addUsersById(usersById, this.keycloak.users().searchByUsername(search, exact));
        this.addUsersById(usersById, this.keycloak.users().searchByEmail(search, exact));
        this.addUsersById(usersById, this.keycloak.users().searchByFirstName(search, exact));
        this.addUsersById(usersById, this.keycloak.users().searchByLastName(search, exact));
        return List.copyOf(usersById.values());
    }

    private void addUsersById(Map<String, UserRepresentation> usersById, List<UserRepresentation> users) {
        for (var user : users) {
            usersById.putIfAbsent(Objects.requireNonNull(user.getId(), "Keycloak user id is required."), user);
        }
    }

    private String toAttributeQuery(Map<String, String> attributes) {
        return attributes.entrySet().stream()
                .map(attribute -> "%s:%s".formatted(attribute.getKey(), attribute.getValue()))
                .collect(Collectors.joining(" "));
    }

    private boolean matches(IdentityUser user, UserSearchCriteria criteria) {
        return this.matchesSearch(criteria.search(), user, criteria.exact())
                && KeycloakFilterMatcher.matchesText(criteria.username(), user.username(), criteria.exact())
                && KeycloakFilterMatcher.matchesText(criteria.email(), user.email(), criteria.exact())
                && KeycloakFilterMatcher.matchesText(criteria.firstName(), user.firstName(), criteria.exact())
                && KeycloakFilterMatcher.matchesText(criteria.lastName(), user.lastName(), criteria.exact())
                && this.matchesEnabled(criteria.enabled(), user.enabled())
                && KeycloakFilterMatcher.matchesAttributes(criteria.attributes(), user.attributes(), criteria.exact());
    }

    private boolean matchesSearch(String filter, IdentityUser user, boolean exact) {
        if (filter == null) {
            return true;
        }

        return KeycloakFilterMatcher.matchesText(filter, user.username(), exact)
                || KeycloakFilterMatcher.matchesText(filter, user.email(), exact)
                || KeycloakFilterMatcher.matchesText(filter, user.firstName(), exact)
                || KeycloakFilterMatcher.matchesText(filter, user.lastName(), exact);
    }

    private boolean matchesEnabled(Boolean requestedValue, Boolean actualValue) {
        return requestedValue == null || requestedValue.equals(actualValue);
    }
}
