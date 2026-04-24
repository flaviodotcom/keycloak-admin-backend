package io.github.flaviodotcom.resources;

import io.github.flaviodotcom.domain.identity.gateway.IdentityUserAttributeGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityUserAttribute;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class UserAttributeResourceIT {

    @InjectMock
    IdentityUserAttributeGateway identityUserAttributeGateway;

    @Test
    void givenValidRequest_WhenCreateAttribute_ThenReturnCreatedResponse() {
        when(this.identityUserAttributeGateway.createAttribute(argThat(command ->
                "cpf".equals(command.name())
                        && "CPF".equals(command.displayName().get("pt-BR"))
                        && Boolean.TRUE.equals(command.insensitive())
                        && Boolean.FALSE.equals(command.required())
                        && Boolean.FALSE.equals(command.multivalued())
        ))).thenReturn(new IdentityUserAttribute("cpf", Map.of("pt-BR", "CPF"), true, false, false));

        given()
                .contentType("application/json")
                .body("""
                        {
                          "name": "cpf",
                          "displayName": {
                            "pt-BR": "CPF"
                          },
                          "insensitive": true
                        }
                        """)
                .when()
                .post("/v1/users/attributes")
                .then()
                .statusCode(201)
                .header("Location", equalTo("http://localhost:8081/v1/users/attributes/cpf"))
                .body("name", equalTo("cpf"))
                .body("displayName.pt-BR", equalTo("CPF"))
                .body("insensitive", equalTo(true))
                .body("required", equalTo(false))
                .body("multivalued", equalTo(false));

        verify(this.identityUserAttributeGateway).createAttribute(argThat(command ->
                "cpf".equals(command.name())
                        && "CPF".equals(command.displayName().get("pt-BR"))
                        && Boolean.TRUE.equals(command.insensitive())
                        && Boolean.FALSE.equals(command.required())
                        && Boolean.FALSE.equals(command.multivalued())
        ));
    }
}
