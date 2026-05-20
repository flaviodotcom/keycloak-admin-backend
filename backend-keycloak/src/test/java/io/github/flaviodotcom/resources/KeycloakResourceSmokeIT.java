package io.github.flaviodotcom.resources;

import io.github.flaviodotcom.config.AbstractIntegrationTest;
import io.github.flaviodotcom.config.WithKeycloakTestContainerProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestProfile(WithKeycloakTestContainerProfile.class)
class KeycloakResourceSmokeIT extends AbstractIntegrationTest {

    private static final String RUN_ID = UUID.randomUUID().toString().substring(0, 8);
    private static final String USERNAME = "it-user-" + RUN_ID;
    private static final String USER_EMAIL = USERNAME + "@example.com";
    private static final String GROUP_NAME = "it-group-" + RUN_ID;
    private static final String ROLE_NAME = "it-role-" + RUN_ID;
    private static final String SMOKE_CLIENT_ID = "it-client";
    private static final String SMOKE_CLIENT_ROLE = "it-client-role";

    @Test
    void givenKeycloakContainer_WhenCreateSearchUpdatePatchAndDeleteUser_ThenUseRealKeycloakAdminApi() {
        String userId = given()
                .contentType("application/json")
                .body("""
                        {
                          "username": "%s",
                          "email": "%s",
                          "firstName": "Integration",
                          "lastName": "User",
                          "enabled": true,
                          "emailVerified": false
                        }
                        """.formatted(USERNAME, USER_EMAIL))
                .when()
                .post("/v1/users")
                .then()
                .statusCode(201)
                .body("id", not(emptyOrNullString()))
                .body("username", equalTo(USERNAME))
                .body("email", equalTo(USER_EMAIL))
                .extract()
                .path("id");

        given()
                .when()
                .get("/v1/users?username={username}&exact=true", USERNAME)
                .then()
                .statusCode(200)
                .body("content[0].id", equalTo(userId))
                .body("content[0].username", equalTo(USERNAME))
                .body("totalElements", equalTo(1));

        given()
                .when()
                .get("/v1/users/{id}", userId)
                .then()
                .statusCode(200)
                .body("id", equalTo(userId))
                .body("username", equalTo(USERNAME));

        given()
                .contentType("application/json")
                .body("""
                        {
                          "username": "%s",
                          "email": "%s",
                          "firstName": "Integration",
                          "lastName": "Updated",
                          "enabled": true,
                          "emailVerified": true
                        }
                        """.formatted(USERNAME, USER_EMAIL))
                .when()
                .put("/v1/users/{id}", userId)
                .then()
                .statusCode(200)
                .body("lastName", equalTo("Updated"))
                .body("emailVerified", equalTo(true));

        given()
                .contentType("application/json")
                .body("""
                        {
                          "firstName": "Patched"
                        }
                        """)
                .when()
                .patch("/v1/users/{id}", userId)
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Patched"))
                .body("lastName", equalTo("Updated"));

        given()
                .contentType("application/json")
                .body("""
                        {
                          "username": "%s",
                          "email": "%s",
                          "firstName": "Integration",
                          "lastName": "Duplicate"
                        }
                        """.formatted(USERNAME, USER_EMAIL))
                .when()
                .post("/v1/users")
                .then()
                .statusCode(409)
                .body("detail", containsString("already exists"));

        given()
                .when()
                .delete("/v1/users/{id}", userId)
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/v1/users/{id}", userId)
                .then()
                .statusCode(404);
    }

    @Test
    void givenKeycloakContainer_WhenManageUserMembershipRolesCredentialsAndSessions_ThenUseRealKeycloakAdminApi() {
        var username = "it-access-user-" + RUN_ID;
        var email = username + "@example.com";
        var groupName = "it-access-group-" + RUN_ID;
        var roleName = "it-access-role-" + RUN_ID;
        String userId = null;
        String groupId = null;
        String roleId = null;

        try {
            groupId = given()
                    .contentType("application/json")
                    .body("""
                            {
                              "name": "%s"
                            }
                            """.formatted(groupName))
                    .when()
                    .post("/v1/groups")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id");

            roleId = given()
                    .contentType("application/json")
                    .body("""
                            {
                              "name": "%s"
                            }
                            """.formatted(roleName))
                    .when()
                    .post("/v1/roles")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id");

            userId = given()
                    .contentType("application/json")
                    .body("""
                            {
                              "username": "%s",
                              "email": "%s",
                              "enabled": true
                            }
                            """.formatted(username, email))
                    .when()
                    .post("/v1/users")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id");

            given()
                    .when()
                    .post("/v1/users/{id}/groups/{groupId}", userId, groupId)
                    .then()
                    .statusCode(204);

            given()
                    .when()
                    .get("/v1/users/{id}?includeGroups=true", userId)
                    .then()
                    .statusCode(200)
                    .body("groups[0].id", equalTo(groupId));

            given()
                    .when()
                    .post("/v1/users/{id}/roles/realm/{roleName}", userId, roleName)
                    .then()
                    .statusCode(204);

            given()
                    .when()
                    .post("/v1/users/{id}/roles/clients/{clientId}/{roleName}", userId, SMOKE_CLIENT_ID, SMOKE_CLIENT_ROLE)
                    .then()
                    .statusCode(204);

            given()
                    .when()
                    .post("/v1/groups/{id}/roles/realm/{roleName}", groupId, roleName)
                    .then()
                    .statusCode(204);

            given()
                    .when()
                    .post("/v1/groups/{id}/roles/clients/{clientId}/{roleName}", groupId, SMOKE_CLIENT_ID, SMOKE_CLIENT_ROLE)
                    .then()
                    .statusCode(204);

            given()
                    .contentType("application/json")
                    .body("""
                            {
                              "value": "ChangeMe123!",
                              "temporary": true
                            }
                            """)
                    .when()
                    .put("/v1/users/{id}/password", userId)
                    .then()
                    .statusCode(204);

            given()
                    .contentType("application/json")
                    .body("""
                            {
                              "actions": ["UPDATE_PASSWORD"]
                            }
                            """)
                    .when()
                    .put("/v1/users/{id}/required-actions", userId)
                    .then()
                    .statusCode(204);

            given()
                    .when()
                    .get("/v1/users/{id}/sessions", userId)
                    .then()
                    .statusCode(200);

            given()
                    .when()
                    .delete("/v1/users/{id}/sessions", userId)
                    .then()
                    .statusCode(204);

            given()
                    .when()
                    .delete("/v1/users/{id}/groups/{groupId}", userId, groupId)
                    .then()
                    .statusCode(204);

            given()
                    .when()
                    .get("/v1/users/{id}?includeGroups=true", userId)
                    .then()
                    .statusCode(200)
                    .body("groups", nullValue());
        } finally {
            if (userId != null) {
                given()
                        .when()
                        .delete("/v1/users/{id}", userId);
            }
            if (roleId != null) {
                given()
                        .when()
                        .delete("/v1/roles/{id}", roleId);
            }
            if (groupId != null) {
                given()
                        .when()
                        .delete("/v1/groups/{id}", groupId);
            }
        }
    }

    @Test
    void givenKeycloakContainer_WhenCreateSearchUpdateAndDeleteGroup_ThenUseRealKeycloakAdminApi() {
        var groupId = given()
                .contentType("application/json")
                .body("""
                        {
                          "name": "%s"
                        }
                        """.formatted(GROUP_NAME))
                .when()
                .post("/v1/groups")
                .then()
                .statusCode(201)
                .body("id", not(emptyOrNullString()))
                .body("name", equalTo(GROUP_NAME))
                .extract()
                .path("id");

        given()
                .when()
                .get("/v1/groups?name={name}&exact=true", GROUP_NAME)
                .then()
                .statusCode(200)
                .body("content[0].id", equalTo(groupId))
                .body("totalElements", equalTo(1));

        given()
                .when()
                .get("/v1/groups/{id}", groupId)
                .then()
                .statusCode(200)
                .body("id", equalTo(groupId))
                .body("name", equalTo(GROUP_NAME));

        var updatedGroupName = GROUP_NAME + "-updated";
        given()
                .contentType("application/json")
                .body("""
                        {
                          "name": "%s"
                        }
                        """.formatted(updatedGroupName))
                .when()
                .put("/v1/groups/{id}", groupId)
                .then()
                .statusCode(200)
                .body("name", equalTo(updatedGroupName));

        given()
                .when()
                .delete("/v1/groups/{id}", groupId)
                .then()
                .statusCode(204);
    }

    @Test
    void givenKeycloakContainerAndGroupWithManyMembers_WhenFindGroupMembers_ThenReturnCompletePaginatedResult() {
        var groupName = "it-members-group-" + RUN_ID;
        var userIds = new ArrayList<String>();
        String groupId = null;

        try {
            groupId = given()
                    .contentType("application/json")
                    .body("""
                            {
                              "name": "%s"
                            }
                            """.formatted(groupName))
                    .when()
                    .post("/v1/groups")
                    .then()
                    .statusCode(201)
                    .body("id", not(emptyOrNullString()))
                    .extract()
                    .path("id");

            for (var index = 0; index < 25; index++) {
                var username = "it-member-%s-%02d".formatted(RUN_ID, index);
                String userId = given()
                        .contentType("application/json")
                        .body("""
                                {
                                  "username": "%s",
                                  "email": "%s@example.com",
                                  "firstName": "Member",
                                  "lastName": "%02d",
                                  "enabled": true,
                                  "emailVerified": false,
                                  "groupIds": ["%s"]
                                }
                                """.formatted(username, username, index, groupId))
                        .when()
                        .post("/v1/users")
                        .then()
                        .statusCode(201)
                        .body("id", not(emptyOrNullString()))
                        .extract()
                        .path("id");
                userIds.add(userId);
            }

            given()
                    .when()
                    .get("/v1/groups/{id}/members?size=10&page=0&sortBy=username", groupId)
                    .then()
                    .statusCode(200)
                    .body("content", hasSize(10))
                    .body("totalElements", equalTo(25))
                    .body("totalPages", equalTo(3));

            given()
                    .when()
                    .get("/v1/groups/{id}/members?size=10&page=2&sortBy=username", groupId)
                    .then()
                    .statusCode(200)
                    .body("content", hasSize(5))
                    .body("totalElements", equalTo(25))
                    .body("page", equalTo(2));
        } finally {
            for (var userId : userIds) {
                given()
                        .when()
                        .delete("/v1/users/{id}", userId);
            }
            if (groupId != null) {
                given()
                        .when()
                        .delete("/v1/groups/{id}", groupId);
            }
        }
    }

    @Test
    void givenKeycloakContainer_WhenCreateSearchUpdateAndDeleteRole_ThenUseRealKeycloakAdminApi() {
        var roleId = given()
                .contentType("application/json")
                .body("""
                        {
                          "name": "%s",
                          "description": "Integration role"
                        }
                        """.formatted(ROLE_NAME))
                .when()
                .post("/v1/roles")
                .then()
                .statusCode(201)
                .body("id", not(emptyOrNullString()))
                .body("name", equalTo(ROLE_NAME))
                .extract()
                .path("id");

        given()
                .when()
                .get("/v1/roles?name={name}&exact=true", ROLE_NAME)
                .then()
                .statusCode(200)
                .body("content[0].id", equalTo(roleId))
                .body("totalElements", equalTo(1));

        given()
                .when()
                .get("/v1/roles/{id}", roleId)
                .then()
                .statusCode(200)
                .body("id", equalTo(roleId))
                .body("name", equalTo(ROLE_NAME));

        given()
                .contentType("application/json")
                .body("""
                        {
                          "name": "%s",
                          "description": "Updated integration role"
                        }
                        """.formatted(ROLE_NAME))
                .when()
                .put("/v1/roles/{id}", roleId)
                .then()
                .statusCode(200)
                .body("description", equalTo("Updated integration role"));

        given()
                .when()
                .delete("/v1/roles/{id}", roleId)
                .then()
                .statusCode(204);
    }
}
