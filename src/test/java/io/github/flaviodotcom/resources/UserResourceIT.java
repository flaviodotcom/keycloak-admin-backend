package io.github.flaviodotcom.resources;

import io.github.flaviodotcom.domain.identity.model.IdentityUser;
import io.github.flaviodotcom.domain.identity.gateway.IdentityUserGateway;
import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.exceptions.BusinessException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class UserResourceIT {

    @InjectMock
    IdentityUserGateway identityUserGateway;

    @Test
    void givenQueryParams_WhenFindUsers_ThenForwardCriteriaToGateway() {
        when(this.identityUserGateway.findUsers(any(UserSearchCriteria.class))).thenReturn(List.of(
                new IdentityUser(
                        "user-1",
                        "john",
                        "john@example.com",
                        "John",
                        "Doe",
                        true,
                        true,
                        123L,
                        Map.of("department", List.of("IT"))
                )
        ));

        given()
                .when()
                .get("/v1/users?username=john&enabled=true&exact=true&attr.department=IT")
                .then()
                .statusCode(200)
                .body("[0].username", equalTo("john"))
                .body("[0].attributes.department[0]", equalTo("IT"));

        verify(this.identityUserGateway).findUsers(argThat(criteria ->
                "john".equals(criteria.username())
                        && Boolean.TRUE.equals(criteria.enabled())
                        && Boolean.TRUE.equals(criteria.exact())
                        && "IT".equals(criteria.attributes().get("department"))
        ));
    }

    @Test
    void givenInvalidBooleanParam_WhenFindUsers_ThenReturnBadRequestProblem() {
        given()
                .when()
                .get("/v1/users?enabled=invalid")
                .then()
                .statusCode(400)
                .body("title", equalTo("Bad Request"))
                .body("detail", containsString("enabled"));
    }

    @Test
    void givenBusinessException_WhenFindUsers_ThenReturnUnprocessableEntityProblem() {
        when(this.identityUserGateway.findUsers(any(UserSearchCriteria.class)))
                .thenThrow(new BusinessException("User rule failed."));

        given()
                .when()
                .get("/v1/users")
                .then()
                .statusCode(422)
                .body("title", equalTo("Business rule violation"))
                .body("detail", equalTo("User rule failed."));
    }

    @Test
    void givenId_WhenFindUserById_ThenReturnUser() {
        when(this.identityUserGateway.findUserById("user-1")).thenReturn(new IdentityUser(
                "user-1",
                "john",
                "john@example.com",
                "John",
                "Doe",
                true,
                true,
                123L,
                Map.of()
        ));

        given()
                .when()
                .get("/v1/users/user-1")
                .then()
                .statusCode(200)
                .body("id", equalTo("user-1"))
                .body("username", equalTo("john"));
    }

    @Test
    void givenValidRequest_WhenUpdateUser_ThenReturnUpdatedResponse() {
        when(this.identityUserGateway.updateUser(any(), any())).thenReturn(new IdentityUser(
                "user-1",
                "john.updated",
                "john.updated@example.com",
                "John",
                "Updated",
                true,
                false,
                123L,
                Map.of("department", List.of("IT"))
        ));

        given()
                .contentType("application/json")
                .body("""
                        {
                          "username": "john.updated",
                          "email": "john.updated@example.com",
                          "firstName": "John",
                          "lastName": "Updated",
                          "attributes": {
                            "department": ["IT"]
                          }
                        }
                        """)
                .when()
                .put("/v1/users/user-1")
                .then()
                .statusCode(200)
                .body("id", equalTo("user-1"))
                .body("username", equalTo("john.updated"))
                .body("attributes.department[0]", equalTo("IT"));

        verify(this.identityUserGateway).updateUser(argThat("user-1"::equals), argThat(command ->
                "john.updated".equals(command.username())
                        && "IT".equals(command.attributes().get("department").getFirst())
        ));
    }

    @Test
    void givenId_WhenDeleteUser_ThenReturnNoContent() {
        given()
                .when()
                .delete("/v1/users/user-1")
                .then()
                .statusCode(204);

        verify(this.identityUserGateway).deleteUser("user-1");
    }

    @Test
    void givenWebApplicationException_WhenCreateUser_ThenReturnOriginalStatusAndBody() {
        when(this.identityUserGateway.createUser(any()))
                .thenThrow(new WebApplicationException("{\"error\":\"User already exists\"}", 409));

        given()
                .contentType("application/json")
                .body("""
                        {
                          "username": "john"
                        }
                        """)
                .when()
                .post("/v1/users")
                .then()
                .statusCode(409)
                .body("title", equalTo("Conflict"))
                .body("detail", containsString("User already exists"));
    }

    @Test
    void givenUnhandledException_WhenFindUsers_ThenReturnInternalServerErrorProblem() {
        when(this.identityUserGateway.findUsers(any(UserSearchCriteria.class)))
                .thenThrow(new IllegalStateException("Unexpected failure."));

        given()
                .when()
                .get("/v1/users")
                .then()
                .statusCode(500)
                .body("title", equalTo("Internal server error"))
                .body("detail", equalTo("Unexpected failure."));
    }
}
