package io.github.flaviodotcom.resources;

import io.github.flaviodotcom.domain.identity.model.IdentityGroup;
import io.github.flaviodotcom.domain.identity.model.IdentityUser;
import io.github.flaviodotcom.domain.identity.gateway.IdentityMembershipGateway;
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
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class UserResourceIT {

    @InjectMock
    IdentityUserGateway identityUserGateway;

    @InjectMock
    IdentityMembershipGateway identityMembershipGateway;

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
                .body("content[0].username", equalTo("john"))
                .body("content[0].attributes.department[0]", equalTo("IT"))
                .body("content[0].groups", nullValue())
                .body("page", equalTo(0))
                .body("size", equalTo(10))
                .body("totalElements", equalTo(1))
                .body("totalPages", equalTo(1));

        verify(this.identityUserGateway).findUsers(argThat(criteria ->
                "john".equals(criteria.username())
                        && Boolean.TRUE.equals(criteria.enabled())
                        && Boolean.TRUE.equals(criteria.exact())
                        && "IT".equals(criteria.attributes().get("department"))
        ));
    }

    @Test
    void givenIncludeGroups_WhenFindUsers_ThenReturnGroups() {
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
                        Map.of()
                )
        ));
        when(this.identityMembershipGateway.findUsersGroups(List.of("user-1")))
                .thenReturn(Map.of("user-1", List.of(
                        new IdentityGroup("group-1", "Financeiro", "/Financeiro", Map.of()),
                        new IdentityGroup("group-2", "TI", "/TI", Map.of())
                )));

        given()
                .when()
                .get("/v1/users?includeGroups=true")
                .then()
                .statusCode(200)
                .body("content[0].groups[0].id", equalTo("group-1"))
                .body("content[0].groups[0].name", equalTo("Financeiro"))
                .body("content[0].groups[0].path", equalTo("/Financeiro"))
                .body("content[0].groups[1].id", equalTo("group-2"))
                .body("content[0].groups[1].name", equalTo("TI"));

        verify(this.identityUserGateway).findUsers(any(UserSearchCriteria.class));
        verify(this.identityMembershipGateway).findUsersGroups(List.of("user-1"));
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
    void givenInvalidPageParam_WhenFindUsers_ThenReturnBadRequestProblem() {
        given()
                .when()
                .get("/v1/users?page=-1")
                .then()
                .statusCode(400)
                .body("title", equalTo("Bad Request"))
                .body("detail", equalTo("Query param 'page' must be greater than or equal to 0."));
    }

    @Test
    void givenPaginationParams_WhenFindUsers_ThenSortAndPageAfterGatewayResult() {
        when(this.identityUserGateway.findUsers(any(UserSearchCriteria.class))).thenReturn(List.of(
                user("user-1", "maria"),
                user("user-2", "ana"),
                user("user-3", "jose")
        ));

        given()
                .when()
                .get("/v1/users?page=1&size=1&sortBy=username&sort=asc")
                .then()
                .statusCode(200)
                .body("content[0].id", equalTo("user-3"))
                .body("content[0].username", equalTo("jose"))
                .body("page", equalTo(1))
                .body("size", equalTo(1))
                .body("totalElements", equalTo(3))
                .body("totalPages", equalTo(3))
                .body("first", equalTo(false))
                .body("last", equalTo(false));
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
    void givenInvalidEmail_WhenCreateUser_ThenReturnValidationProblem() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "username": "john",
                          "email": "invalid-email"
                        }
                        """)
                .when()
                .post("/v1/users")
                .then()
                .statusCode(400)
                .body("messages[0].name", equalTo("email"))
                .body("messages[0].message", equalTo("email must be valid"));
    }

    @Test
    void givenInvalidEmail_WhenPatchUser_ThenReturnValidationProblem() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "email": "invalid-email"
                        }
                        """)
                .when()
                .patch("/v1/users/user-1")
                .then()
                .statusCode(400)
                .body("messages[0].name", equalTo("email"))
                .body("messages[0].message", equalTo("email must be valid"));
    }

    @Test
    void givenEmptyAttributeValues_WhenCreateUser_ThenReturnValidationProblem() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "username": "john",
                          "attributes": {
                            "cpf": []
                          }
                        }
                        """)
                .when()
                .post("/v1/users")
                .then()
                .statusCode(400)
                .body("messages[0].name", equalTo("attributes"))
                .body("messages[0].message", equalTo("attribute values cannot be empty"));
    }

    @Test
    void givenBlankGroupId_WhenCreateUser_ThenReturnValidationProblem() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "username": "john",
                          "groupIds": [""]
                        }
                        """)
                .when()
                .post("/v1/users")
                .then()
                .statusCode(400)
                .body("messages[0].message", equalTo("groupId is required"));
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
                .body("username", equalTo("john"))
                .body("groups", nullValue());
    }

    @Test
    void givenIncludeGroups_WhenFindUserById_ThenReturnGroups() {
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
        when(this.identityMembershipGateway.findUserGroups("user-1")).thenReturn(List.of(
                new IdentityGroup("group-1", "Financeiro", "/Financeiro", Map.of())
        ));

        given()
                .when()
                .get("/v1/users/user-1?includeGroups=true")
                .then()
                .statusCode(200)
                .body("id", equalTo("user-1"))
                .body("groups[0].id", equalTo("group-1"))
                .body("groups[0].name", equalTo("Financeiro"))
                .body("groups[0].path", equalTo("/Financeiro"));

        verify(this.identityMembershipGateway).findUserGroups("user-1");
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
    void givenPartialRequest_WhenPatchUser_ThenReturnPatchedResponse() {
        when(this.identityUserGateway.patchUser(any(), any())).thenReturn(new IdentityUser(
                "user-1",
                "john",
                "john@example.com",
                "Johnny",
                "Doe",
                true,
                true,
                123L,
                Map.of("department", List.of("IT"))
        ));

        given()
                .contentType("application/json")
                .body("""
                        {
                          "firstName": "Johnny"
                        }
                        """)
                .when()
                .patch("/v1/users/user-1")
                .then()
                .statusCode(200)
                .body("id", equalTo("user-1"))
                .body("username", equalTo("john"))
                .body("firstName", equalTo("Johnny"))
                .body("attributes.department[0]", equalTo("IT"));

        verify(this.identityUserGateway).patchUser(argThat("user-1"::equals), argThat(command ->
                "Johnny".equals(command.firstName())
                        && command.username() == null
                        && command.attributes() == null
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

    private IdentityUser user(String id, String username) {
        return new IdentityUser(
                id,
                username,
                "%s@example.com".formatted(username),
                username,
                "User",
                true,
                true,
                123L,
                Map.of()
        );
    }
}
