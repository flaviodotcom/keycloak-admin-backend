package io.github.flaviodotcom.infrastructure.keycloak;

import io.github.flaviodotcom.domain.identity.CreateIdentityUserCommand;
import io.github.flaviodotcom.domain.identity.IdentityUser;
import io.github.flaviodotcom.domain.identity.IdentityUserGateway;
import io.github.flaviodotcom.domain.identity.UserSearchCriteria;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.AllArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakUserGateway implements IdentityUserGateway {

    private static final int FIRST_RESULT = 0;
    private static final int MAX_RESULTS = Integer.MAX_VALUE;

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
            return this.keycloak.users().search(criteria.search(), criteria.exact(), FIRST_RESULT, MAX_RESULTS);
        }

        if (criteria.hasStructuredFilters()) {
            return this.keycloak.users().search(
                    criteria.username(),
                    criteria.firstName(),
                    criteria.lastName(),
                    criteria.email(),
                    criteria.enabled(),
                    FIRST_RESULT,
                    MAX_RESULTS,
                    false,
                    criteria.exact()
            );
        }

        return this.keycloak.users().list(FIRST_RESULT, MAX_RESULTS);
    }

    private String toAttributeQuery(Map<String, String> attributes) {
        return attributes.entrySet().stream()
                .map(attribute -> "%s:%s".formatted(attribute.getKey(), attribute.getValue()))
                .collect(Collectors.joining(" "));
    }

    private boolean matches(IdentityUser user, UserSearchCriteria criteria) {
        return this.matchesSearch(criteria.search(), user, criteria.exact())
                && this.matchesText(criteria.username(), user.username(), criteria.exact())
                && this.matchesText(criteria.email(), user.email(), criteria.exact())
                && this.matchesText(criteria.firstName(), user.firstName(), criteria.exact())
                && this.matchesText(criteria.lastName(), user.lastName(), criteria.exact())
                && this.matchesEnabled(criteria.enabled(), user.enabled())
                && this.matchesAttributes(criteria.attributes(), user.attributes(), criteria.exact());
    }

    private boolean matchesSearch(String filter, IdentityUser user, boolean exact) {
        if (filter == null) {
            return true;
        }

        return this.matchesText(filter, user.username(), exact)
                || this.matchesText(filter, user.email(), exact)
                || this.matchesText(filter, user.firstName(), exact)
                || this.matchesText(filter, user.lastName(), exact);
    }

    private boolean matchesEnabled(Boolean requestedValue, Boolean actualValue) {
        return requestedValue == null || requestedValue.equals(actualValue);
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
