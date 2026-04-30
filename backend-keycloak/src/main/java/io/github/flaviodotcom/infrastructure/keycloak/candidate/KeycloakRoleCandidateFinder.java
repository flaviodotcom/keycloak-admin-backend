package io.github.flaviodotcom.infrastructure.keycloak.candidate;

import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.github.flaviodotcom.domain.shared.SearchTermBuilder;
import io.github.flaviodotcom.i18n.Messages;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import static io.github.flaviodotcom.infrastructure.keycloak.pagination.KeycloakQueryDefaults.FIRST_RESULT;
import static io.github.flaviodotcom.infrastructure.keycloak.pagination.KeycloakQueryDefaults.MAX_RESULTS;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakRoleCandidateFinder {

    private final KeycloakAdminSupport keycloak;

    public List<RoleRepresentation> findCandidates(RoleSearchCriteria criteria) {
        if (criteria.name() == null || criteria.name().isBlank()) {
            return this.keycloak.roles().list(FIRST_RESULT, MAX_RESULTS, false);
        }

        var rolesById = new LinkedHashMap<String, RoleRepresentation>();
        for (var term : SearchTermBuilder.build(criteria.name())) {
            for (var role : this.keycloak.roles().list(term, FIRST_RESULT, MAX_RESULTS, false)) {
                rolesById.putIfAbsent(Objects.requireNonNull(role.getId(), Messages.getDefault("error.keycloak.role-id.required")), role);
            }
        }
        return List.copyOf(rolesById.values());
    }
}
