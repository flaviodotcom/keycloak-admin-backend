package io.github.flaviodotcom.containers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeycloakTestContainer implements QuarkusTestResourceLifecycleManager {

    private static final String REALM = "backend-users-test";
    private static final String ADMIN_CLIENT_ID = "backend-keycloak-admin";
    private static final String ADMIN_CLIENT_SECRET = "test-secret";
    private static final String REALM_MANAGEMENT_CLIENT_ID = "realm-management";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final List<String> ADMIN_ROLES = List.of(
            "realm-admin",
            "manage-realm",
            "view-realm",
            "manage-users",
            "view-users",
            "query-users",
            "query-groups"
    );

    private KeycloakContainer keycloak;

    @Override
    public Map<String, String> start() {
        this.keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.0")
                .withAdminUsername("admin")
                .withAdminPassword("admin");

        this.keycloak.start();
        this.configureRealm();

        var config = new HashMap<String, String>();
        config.put("keycloak.server-url", this.keycloak.getAuthServerUrl());
        config.put("keycloak.realm", REALM);
        config.put("quarkus.keycloak.admin-client.server-url", this.keycloak.getAuthServerUrl());
        config.put("quarkus.keycloak.admin-client.realm", REALM);
        config.put("quarkus.keycloak.admin-client.client-id", ADMIN_CLIENT_ID);
        config.put("quarkus.keycloak.admin-client.client-secret", ADMIN_CLIENT_SECRET);
        config.put("quarkus.keycloak.admin-client.grant-type", "CLIENT_CREDENTIALS");
        config.put("quarkus.oidc.enabled", "false");
        return config;
    }

    private void configureRealm() {
        var accessToken = this.getMasterAccessToken();
        this.createRealm(accessToken);
        this.createAdminClient(accessToken);
        this.assignAdminClientRoles(accessToken);
    }

    private String getMasterAccessToken() {
        var body = form(Map.of(
                "grant_type", "password",
                "client_id", "admin-cli",
                "username", "admin",
                "password", "admin"
        ));
        var response = this.send(HttpRequest.newBuilder(this.uri("/realms/master/protocol/openid-connect/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build());
        this.ensureStatus(response, 200, "master access token");
        return this.readJson(response.body()).get("access_token").asText();
    }

    private void createRealm(String accessToken) {
        var realm = Map.of(
                "realm", REALM,
                "enabled", true,
                "loginWithEmailAllowed", true,
                "duplicateEmailsAllowed", false,
                "editUsernameAllowed", true,
                "resetPasswordAllowed", true
        );
        var response = this.sendJson(accessToken, "/admin/realms", realm);
        this.ensureStatus(response, 201, "realm");
    }

    private void createAdminClient(String accessToken) {
        var client = Map.ofEntries(
                Map.entry("clientId", ADMIN_CLIENT_ID),
                Map.entry("name", "Backend Keycloak Admin"),
                Map.entry("enabled", true),
                Map.entry("protocol", "openid-connect"),
                Map.entry("publicClient", false),
                Map.entry("secret", ADMIN_CLIENT_SECRET),
                Map.entry("serviceAccountsEnabled", true),
                Map.entry("standardFlowEnabled", false),
                Map.entry("implicitFlowEnabled", false),
                Map.entry("directAccessGrantsEnabled", false),
                Map.entry("authorizationServicesEnabled", false),
                Map.entry("fullScopeAllowed", true)
        );
        var response = this.sendJson(accessToken, "/admin/realms/" + REALM + "/clients", client);
        this.ensureStatus(response, 201, "admin client");
    }

    private void assignAdminClientRoles(String accessToken) {
        var adminClientId = this.findClientId(accessToken, ADMIN_CLIENT_ID);
        var realmManagementClientId = this.findClientId(accessToken, REALM_MANAGEMENT_CLIENT_ID);
        var serviceAccountId = this.findServiceAccountId(accessToken, adminClientId);
        var roles = ADMIN_ROLES.stream()
                .map(roleName -> this.findClientRole(accessToken, realmManagementClientId, roleName))
                .toList();

        var response = this.sendJson(
                accessToken,
                "/admin/realms/" + REALM + "/users/" + serviceAccountId + "/role-mappings/clients/" + realmManagementClientId,
                roles
        );
        this.ensureStatus(response, 204, "admin client role mappings");
    }

    private String findClientId(String accessToken, String clientId) {
        var response = this.sendAuthorizedGet(
                accessToken,
                "/admin/realms/" + REALM + "/clients?clientId=" + encode(clientId)
        );
        this.ensureStatus(response, 200, "client " + clientId);
        return this.readJson(response.body()).get(0).get("id").asText();
    }

    private String findServiceAccountId(String accessToken, String clientId) {
        var response = this.sendAuthorizedGet(
                accessToken,
                "/admin/realms/" + REALM + "/clients/" + clientId + "/service-account-user"
        );
        this.ensureStatus(response, 200, "admin client service account");
        return this.readJson(response.body()).get("id").asText();
    }

    private JsonNode findClientRole(String accessToken, String clientId, String roleName) {
        var response = this.sendAuthorizedGet(
                accessToken,
                "/admin/realms/" + REALM + "/clients/" + clientId + "/roles/" + encode(roleName)
        );
        this.ensureStatus(response, 200, "client role " + roleName);
        return this.readJson(response.body());
    }

    private HttpResponse<String> sendJson(String accessToken, String path, Object body) {
        return this.send(HttpRequest.newBuilder(this.uri(path))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(this.writeJson(body)))
                .build());
    }

    private HttpResponse<String> sendAuthorizedGet(String accessToken, String path) {
        return this.send(HttpRequest.newBuilder(this.uri(path))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build());
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private URI uri(String path) {
        return URI.create(this.keycloak.getAuthServerUrl() + path);
    }

    private JsonNode readJson(String body) {
        try {
            return JSON.readTree(body);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private String writeJson(Object body) {
        try {
            return JSON.writeValueAsString(body);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static String form(Map<String, String> values) {
        return values.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .reduce((left, right) -> left + "&" + right)
                .orElseThrow();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void ensureStatus(HttpResponse<String> response, int expectedStatus, String resource) {
        if (response.statusCode() != expectedStatus) {
            throw new IllegalStateException(
                    "Could not create Keycloak test %s. HTTP status: %d. Body: %s"
                            .formatted(resource, response.statusCode(), response.body())
            );
        }
    }

    @Override
    public void stop() {
        if (this.keycloak != null) {
            this.keycloak.stop();
        }
    }
}
