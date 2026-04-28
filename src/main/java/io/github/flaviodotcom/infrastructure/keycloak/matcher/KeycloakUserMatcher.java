package io.github.flaviodotcom.infrastructure.keycloak.matcher;

import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.domain.identity.model.IdentityUser;
import io.github.flaviodotcom.domain.shared.TextFilterMatcher;
import io.github.flaviodotcom.infrastructure.keycloak.userprofile.KeycloakUserAttributeDefinitions;
import io.github.flaviodotcom.infrastructure.keycloak.userprofile.KeycloakUserAttributeIndex;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakUserMatcher {

    private final KeycloakUserAttributeIndex attributeIndex;

    public boolean matches(IdentityUser user,
                           UserSearchCriteria criteria,
                           KeycloakUserAttributeDefinitions attributeDefinitions) {
        return this.matchesSearch(criteria.search(), user, criteria.exact())
                && TextFilterMatcher.matches(criteria.username(), user.username(), criteria.exact())
                && TextFilterMatcher.matches(criteria.email(), user.email(), criteria.exact())
                && TextFilterMatcher.matches(criteria.firstName(), user.firstName(), criteria.exact())
                && TextFilterMatcher.matches(criteria.lastName(), user.lastName(), criteria.exact())
                && this.matchesEnabled(criteria.enabled(), user.enabled())
                && this.attributeIndex.matches(criteria.attributes(), user.attributes(), criteria.exact(), attributeDefinitions);
    }

    private boolean matchesSearch(String filter, IdentityUser user, boolean exact) {
        if (filter == null) {
            return true;
        }

        return TextFilterMatcher.matches(filter, user.username(), exact)
                || TextFilterMatcher.matches(filter, user.email(), exact)
                || TextFilterMatcher.matches(filter, user.firstName(), exact)
                || TextFilterMatcher.matches(filter, user.lastName(), exact);
    }

    private boolean matchesEnabled(Boolean requestedValue, Boolean actualValue) {
        return requestedValue == null || requestedValue.equals(actualValue);
    }
}
