package io.github.flaviodotcom.resources;

import io.github.flaviodotcom.config.AbstractIntegrationTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class OpenApiResourceIT extends AbstractIntegrationTest {

    @Test
    void givenUnknownRoute_WhenGetRoot_ThenReturnNotFoundWithCorrelationId() {
        given()
                .when()
                .get("/")
                .then()
                .statusCode(404)
                .header("X-Correlation-Id", notNullValue());
    }

    @Test
    void givenStaticOpenApi_WhenGetOpenApi_ThenReturnConfiguredContract() {
        given()
                .basePath("")
                .accept("application/yaml")
                .when()
                .get("/openapi")
                .then()
                .statusCode(200)
                .body(containsString("Keycloak Admin Backend API"))
                .body(containsString("http://localhost:8081/api"))
                .body(containsString("/v1/users/{id}/actions/update-password-email"))
                .body(containsString("/v1/users/{id}/groups/{groupId}"))
                .body(containsString("/v1/users/{id}/roles/clients/{clientId}/{roleName}"))
                .body(containsString("/v1/users/{id}/password"))
                .body(containsString("/v1/users/{id}/sessions/{sessionId}"))
                .body(containsString("/v1/users/attributes/{name}"))
                .body(containsString("/v1/groups/{id}/roles/clients/{clientId}/{roleName}"));
    }
}
