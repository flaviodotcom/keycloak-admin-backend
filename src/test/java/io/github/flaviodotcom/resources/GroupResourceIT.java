package io.github.flaviodotcom.resources;

import io.github.flaviodotcom.domain.identity.criteria.GroupSearchCriteria;
import io.github.flaviodotcom.domain.identity.model.IdentityGroup;
import io.github.flaviodotcom.domain.identity.gateway.IdentityGroupGateway;
import io.github.flaviodotcom.domain.identity.gateway.IdentityMembershipGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityUser;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class GroupResourceIT {

    @InjectMock
    IdentityGroupGateway identityGroupGateway;

    @InjectMock
    IdentityMembershipGateway identityMembershipGateway;

    @Test
    void givenQueryParams_WhenFindGroups_ThenMapKnownAndAttributeFilters() {
        when(this.identityGroupGateway.findGroups(any(GroupSearchCriteria.class))).thenReturn(List.of(
                new IdentityGroup("group-1", "Backoffice", "/Backoffice", Map.of("state", List.of("RJ")))
        ));

        given()
                .when()
                .get("/v1/groups?search=back&exact=false&attr.state=RJ")
                .then()
                .statusCode(200)
                .body("[0].id", equalTo("group-1"))
                .body("[0].attributes.state[0]", equalTo("RJ"));

        verify(this.identityGroupGateway).findGroups(argThat(criteria ->
                "back".equals(criteria.search())
                        && Boolean.FALSE.equals(criteria.exact())
                        && "RJ".equals(criteria.attributes().get("state"))
        ));
    }

    @Test
    void givenValidRequest_WhenCreateGroup_ThenReturnCreatedResponse() {
        when(this.identityGroupGateway.createGroup(any())).thenReturn(
                new IdentityGroup("group-99", "Operations", "/Operations", Map.of())
        );

        given()
                .contentType("application/json")
                .body("""
                        {
                          "name": "Operations"
                        }
                        """)
                .when()
                .post("/v1/groups")
                .then()
                .statusCode(201)
                .header("Location", equalTo("http://localhost:8081/v1/groups/group-99"))
                .body("name", equalTo("Operations"));
    }

    @Test
    void givenId_WhenFindGroupById_ThenReturnGroup() {
        when(this.identityGroupGateway.findGroupById("group-1")).thenReturn(
                new IdentityGroup("group-1", "Backoffice", "/Backoffice", Map.of("state", List.of("RJ")))
        );

        given()
                .when()
                .get("/v1/groups/group-1")
                .then()
                .statusCode(200)
                .body("id", equalTo("group-1"))
                .body("name", equalTo("Backoffice"))
                .body("attributes.state[0]", equalTo("RJ"));
    }

    @Test
    void givenId_WhenFindGroupMembers_ThenReturnUsers() {
        when(this.identityMembershipGateway.findGroupMembers("group-1")).thenReturn(List.of(
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

        given()
                .when()
                .get("/v1/groups/group-1/members")
                .then()
                .statusCode(200)
                .body("[0].id", equalTo("user-1"))
                .body("[0].username", equalTo("john"));

        verify(this.identityMembershipGateway).findGroupMembers("group-1");
    }

    @Test
    void givenValidRequest_WhenUpdateGroup_ThenReturnUpdatedGroup() {
        when(this.identityGroupGateway.updateGroup(any(), any())).thenReturn(
                new IdentityGroup("group-1", "Operations", "/Operations", Map.of("state", List.of("SP")))
        );

        given()
                .contentType("application/json")
                .body("""
                        {
                          "name": "Operations",
                          "attributes": {
                            "state": ["SP"]
                          }
                        }
                        """)
                .when()
                .put("/v1/groups/group-1")
                .then()
                .statusCode(200)
                .body("id", equalTo("group-1"))
                .body("name", equalTo("Operations"))
                .body("attributes.state[0]", equalTo("SP"));

        verify(this.identityGroupGateway).updateGroup(argThat("group-1"::equals), argThat(command ->
                "Operations".equals(command.name())
                        && "SP".equals(command.attributes().get("state").getFirst())
        ));
    }

    @Test
    void givenId_WhenDeleteGroup_ThenReturnNoContent() {
        given()
                .when()
                .delete("/v1/groups/group-1")
                .then()
                .statusCode(204);

        verify(this.identityGroupGateway).deleteGroup("group-1");
    }
}
