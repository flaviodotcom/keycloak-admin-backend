package io.github.flaviodotcom.infrastructure.keycloak.matcher;

import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.domain.identity.model.IdentityUser;
import io.github.flaviodotcom.domain.shared.AttributeFilterMatcher;
import io.github.flaviodotcom.domain.shared.TextFilterMatcher;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KeycloakUserMatcher {

    public boolean matches(IdentityUser user, UserSearchCriteria criteria) {
        return this.matchesSearch(criteria.search(), user, criteria.exact())
                && TextFilterMatcher.matches(criteria.username(), user.username(), criteria.exact())
                && TextFilterMatcher.matches(criteria.email(), user.email(), criteria.exact())
                && TextFilterMatcher.matches(criteria.firstName(), user.firstName(), criteria.exact())
                && TextFilterMatcher.matches(criteria.lastName(), user.lastName(), criteria.exact())
                && this.matchesEnabled(criteria.enabled(), user.enabled())
                && AttributeFilterMatcher.matches(criteria.attributes(), user.attributes(), criteria.exact());
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
