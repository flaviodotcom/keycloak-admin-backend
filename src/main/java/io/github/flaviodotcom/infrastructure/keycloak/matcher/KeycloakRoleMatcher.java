package io.github.flaviodotcom.infrastructure.keycloak.matcher;

import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.github.flaviodotcom.domain.identity.model.IdentityRole;
import io.github.flaviodotcom.domain.shared.TextFilterMatcher;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KeycloakRoleMatcher {

    public boolean matches(IdentityRole role, RoleSearchCriteria criteria) {
        return TextFilterMatcher.matches(criteria.name(), role.name(), criteria.exact());
    }
}
