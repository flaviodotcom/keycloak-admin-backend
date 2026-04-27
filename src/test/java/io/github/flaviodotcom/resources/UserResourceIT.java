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
    void givenWildcardLanguage_WhenFindUsersWithInvalidBooleanParam_ThenReturnDefaultEnglishProblem() {
        given()
                .header("Accept-Language", "*")
                .when()
                .get("/v1/users?enabled=invalid")
                .then()
                .statusCode(400)
                .body("title", equalTo("Bad Request"))
                .body("detail", equalTo("Query param 'enabled' must be 'true' or 'false'."));
    }

    @Test
    void givenPortugueseLanguage_WhenFindUsersWithInvalidBooleanParam_ThenReturnLocalizedProblem() {
        given()
                .header("Accept-Language", "pt-BR")
                .when()
                .get("/v1/users?enabled=invalid")
                .then()
                .statusCode(400)
                .body("title", equalTo("Requisição inválida"))
                .body("detail", equalTo("Parâmetro de consulta 'enabled' deve ser 'true' ou 'false'."));
    }

    @Test
    void givenWildcardLanguage_WhenCreateUserWithoutUsername_ThenReturnDefaultEnglishValidationProblem() {
        given()
                .header("Accept-Language", "*")
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/v1/users")
                .then()
                .statusCode(400)
                .body("title", equalTo("Invalid request data"))
                .body("detail", equalTo("One or more fields are invalid."))
                .body("messages[0].name", equalTo("username"))
                .body("messages[0].message", equalTo("username is required"));
    }

    @Test
    void givenEnglishLanguage_WhenCreateUserWithoutUsername_ThenReturnEnglishValidationProblem() {
        given()
                .header("Accept-Language", "en")
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/v1/users")
                .then()
                .statusCode(400)
                .body("title", equalTo("Invalid request data"))
                .body("detail", equalTo("One or more fields are invalid."))
                .body("messages[0].name", equalTo("username"))
                .body("messages[0].message", equalTo("username is required"));
    }

    @Test
    void givenPortugueseLanguage_WhenCreateUserWithoutUsername_ThenReturnLocalizedValidationProblem() {
        given()
                .header("Accept-Language", "pt-BR")
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/v1/users")
                .then()
                .statusCode(400)
                .body("title", equalTo("Dados da requisição inválidos"))
                .body("detail", equalTo("Um ou mais campos estão inválidos."))
                .body("messages[0].name", equalTo("username"))
                .body("messages[0].message", equalTo("username é obrigatório"));
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
    void givenGroupIds_WhenCreateUser_ThenForwardGroupsToGateway() {
        when(this.identityUserGateway.createUser(any())).thenReturn(new IdentityUser(
                "user-1",
                "pedro.teste",
                "pedro.teste@email.com",
                "Pedro",
                "Teste",
                true,
                false,
                123L,
                Map.of()
        ));

        given()
                .contentType("application/json")
                .body("""
                        {
                          "username": "pedro.teste",
                          "email": "pedro.teste@email.com",
                          "firstName": "Pedro",
                          "lastName": "Teste",
                          "groupIds": ["group-1", "group-2"]
                        }
                        """)
                .when()
                .post("/v1/users")
                .then()
                .statusCode(201)
                .header("Location", equalTo("http://localhost:8081/v1/users/user-1"))
                .body("id", equalTo("user-1"))
                .body("username", equalTo("pedro.teste"));

        verify(this.identityUserGateway).createUser(argThat(command ->
                "pedro.teste".equals(command.username())
                        && List.of("group-1", "group-2").equals(command.groupIds())
        ));
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
