package io.github.flaviodotcom.infrastructure.keycloak;

import io.github.flaviodotcom.domain.identity.CreateIdentityRoleCommand;
import io.github.flaviodotcom.domain.identity.IdentityRole;
import io.github.flaviodotcom.domain.identity.IdentityRoleGateway;
import io.github.flaviodotcom.domain.identity.RoleSearchCriteria;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Locale;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakRoleGateway implements IdentityRoleGateway {

    private static final int FIRST_RESULT = 0;
    private static final int MAX_RESULTS = Integer.MAX_VALUE;

    private final KeycloakAdminSupport keycloak;
    private final KeycloakRepresentationMapper mapper;

    @Override
    public List<IdentityRole> findRoles(RoleSearchCriteria criteria) {
        try {
            var roles = criteria.name() == null
                    ? this.keycloak.roles().list(FIRST_RESULT, MAX_RESULTS, false)
                    : this.keycloak.roles().list(criteria.name(), FIRST_RESULT, MAX_RESULTS, false);

            return roles.stream()
                    .map(this.mapper::toIdentityRole)
                    .filter(role -> this.matches(role, criteria))
                    .toList();
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    @Override
    public IdentityRole createRole(CreateIdentityRoleCommand command) {
        try {
            this.keycloak.roles().create(this.mapper.toRoleRepresentation(command));
            return this.mapper.toIdentityRole(this.keycloak.roles().get(command.name()).toRepresentation());
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    private boolean matches(IdentityRole role, RoleSearchCriteria criteria) {
        if (criteria.name() == null) {
            return true;
        }

        if (role.name() == null) {
            return false;
        }

        var normalizedFilter = criteria.name().toLowerCase(Locale.ROOT);
        var normalizedName = role.name().toLowerCase(Locale.ROOT);
        return criteria.exact()
                ? normalizedName.equals(normalizedFilter)
                : normalizedName.contains(normalizedFilter);
    }
}
