package io.github.flaviodotcom.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "quarkus.keycloak.admin-client")
public interface KeycloakAdminClientConfig {

    String realm();
}
