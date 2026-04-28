package io.github.flaviodotcom.infrastructure.keycloak.cache;

import io.github.flaviodotcom.exceptions.BusinessException;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakClientCatalog {

    private final KeycloakAdminSupport keycloak;

    @CacheResult(cacheName = KeycloakCacheNames.CLIENT_UUIDS)
    public String findClientUuid(String clientId) {
        var clients = this.keycloak.realm().clients().findByClientId(clientId);
        if (clients.isEmpty()) {
            throw BusinessException.localized("error.keycloak.client.not-found", clientId);
        }
        if (clients.size() > 1) {
            throw BusinessException.localized("error.keycloak.client.ambiguous", clientId);
        }

        return clients.getFirst().getId();
    }
}
