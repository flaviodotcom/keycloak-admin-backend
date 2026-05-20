package io.github.flaviodotcom.resources;

import io.github.flaviodotcom.config.WithKeycloakTestContainerProfile;
import io.github.flaviodotcom.service.events.RequestActorResolver;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@TestProfile(WithKeycloakTestContainerProfile.class)
class RequireActorHeaderIT {

    @Test
    void givenIdentityEventsEnabled_WhenHeaderIsMissing_ThenReturnBadRequest() {
        given()
                .header(RequestActorResolver.ACTOR_HEADER, "")
                .contentType("application/json")
                .body("""
                        {
                          "username": "john"
                        }
                        """)
                .when()
                .post("/v1/users")
                .then()
                .statusCode(400)
                .body("title", equalTo("Bad Request"))
                .body(
                        "detail",
                        equalTo("X-Actor-Id header is required when identity events are enabled.")
                );
    }

    @Test
    void givenIdentityEventsEnabled_WhenHeaderIsPresent_ThenDoNotBlockRequest() {
        given()
                .header(RequestActorResolver.ACTOR_HEADER, "integration-test@example.com")
                .contentType("application/json")
                .body("""
                    {
                      "username": ""
                    }
                    """)
                .when()
                .post("/v1/users")
                .then()
                .statusCode(400)
                .body("title", equalTo("Invalid request data"));
    }

}
