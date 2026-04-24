package io.github.flaviodotcom.resources;

import io.github.flaviodotcom.domain.identity.model.IdentityRole;
import io.github.flaviodotcom.domain.identity.gateway.IdentityRoleGateway;
import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class RoleResourceIT {

    @InjectMock
    IdentityRoleGateway identityRoleGateway;

    @Test
    void givenNameFilter_WhenFindRoles_ThenForwardCriteria() {
        when(this.identityRoleGateway.findRoles(any(RoleSearchCriteria.class))).thenReturn(List.of(
                new IdentityRole("role-1", "manage-users", "Manage users", false, false, "realm")
        ));

        given()
                .when()
                .get("/v1/roles?name=manage&exact=false")
                .then()
                .statusCode(200)
                .body("[0].name", equalTo("manage-users"));

        verify(this.identityRoleGateway).findRoles(argThat(criteria ->
                "manage".equals(criteria.name())
                        && Boolean.FALSE.equals(criteria.exact())
        ));
    }

    @Test
    void givenValidRequest_WhenCreateRole_ThenReturnCreatedResponse() {
        when(this.identityRoleGateway.createRole(any())).thenReturn(
                new IdentityRole("role-99", "read-users", "Read users", false, false, "realm")
        );

        given()
                .contentType("application/json")
                .body("""
                        {
                          "name": "read-users",
                          "description": "Read users"
                        }
                        """)
                .when()
                .post("/v1/roles")
                .then()
                .statusCode(201)
                .header("Location", equalTo("http://localhost:8081/v1/roles/role-99"))
                .body("name", equalTo("read-users"));
    }
}
