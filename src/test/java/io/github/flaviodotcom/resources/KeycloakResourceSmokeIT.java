package io.github.flaviodotcom.resources;

import io.github.flaviodotcom.config.WithKeycloakTestContainerProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;

@QuarkusTest
@TestProfile(WithKeycloakTestContainerProfile.class)
class KeycloakResourceSmokeIT {

    private static final String RUN_ID = UUID.randomUUID().toString().substring(0, 8);
    private static final String USERNAME = "it-user-" + RUN_ID;
    private static final String USER_EMAIL = USERNAME + "@example.com";
    private static final String GROUP_NAME = "it-group-" + RUN_ID;
    private static final String ROLE_NAME = "it-role-" + RUN_ID;

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
