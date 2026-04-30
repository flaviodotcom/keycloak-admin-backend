package io.github.flaviodotcom.infrastructure.keycloak.matcher;

import io.github.flaviodotcom.domain.identity.criteria.GroupSearchCriteria;
import io.github.flaviodotcom.domain.identity.model.IdentityGroup;
import io.github.flaviodotcom.domain.shared.AttributeFilterMatcher;
import io.github.flaviodotcom.domain.shared.TextFilterMatcher;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KeycloakGroupMatcher {

    public boolean matches(IdentityGroup group, GroupSearchCriteria criteria) {
        return this.matchesSearch(criteria.search(), group, criteria.exact())
                && TextFilterMatcher.matches(criteria.name(), group.name(), criteria.exact())
                && AttributeFilterMatcher.matches(criteria.attributes(), group.attributes(), criteria.exact());
    }

    private boolean matchesSearch(String filter, IdentityGroup group, boolean exact) {
        if (filter == null) {
            return true;
        }

        return TextFilterMatcher.matches(filter, group.name(), exact)
                || TextFilterMatcher.matches(filter, group.path(), exact);
    }
}
