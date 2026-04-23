package io.github.flaviodotcom.infrastructure.keycloak;

import io.github.flaviodotcom.config.KeycloakAdminClientConfig;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakAdminSupport {

    private final Keycloak keycloak;
    private final KeycloakAdminClientConfig config;

    public RealmResource realm() {
        return this.keycloak.realm(this.config.realm());
    }

    public UsersResource users() {
        return this.realm().users();
    }

    public GroupsResource groups() {
        return this.realm().groups();
    }

    public RolesResource roles() {
        return this.realm().roles();
    }
}
