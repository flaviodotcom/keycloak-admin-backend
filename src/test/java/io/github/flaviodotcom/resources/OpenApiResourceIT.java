package io.github.flaviodotcom.resources;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class OpenApiResourceIT {

    @Test
    void givenStaticOpenApi_WhenGetOpenApi_ThenReturnConfiguredContract() {
        given()
                .accept("application/yaml")
                .when()
                .get("/openapi")
                .then()
                .statusCode(200)
                .body(containsString("Keycloak Admin Backend API"))
                .body(containsString("/v1/users/{id}/actions/update-password-email"))
                .body(containsString("/v1/users/attributes"));
    }
}
