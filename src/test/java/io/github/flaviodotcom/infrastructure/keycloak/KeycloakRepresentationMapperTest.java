package io.github.flaviodotcom.infrastructure.keycloak;

import io.github.flaviodotcom.domain.identity.CreateIdentityUserCommand;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakRepresentationMapperTest {

    private final KeycloakRepresentationMapper mapper = new KeycloakRepresentationMapper();

    @Test
    void givenUserRepresentation_WhenToIdentityUser_ThenMapCoreFieldsAndAttributes() {
        var userRepresentation = new UserRepresentation();
        userRepresentation.setId("user-1");
        userRepresentation.setUsername("john");
        userRepresentation.setEmail("john@example.com");
        userRepresentation.setFirstName("John");
        userRepresentation.setLastName("Doe");
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(false);
        userRepresentation.setCreatedTimestamp(99L);
        userRepresentation.setAttributes(Map.of("department", List.of("IT")));

        var identityUser = this.mapper.toIdentityUser(userRepresentation);

        assertEquals("user-1", identityUser.id());
        assertEquals("john", identityUser.username());
        assertEquals("IT", identityUser.attributes().get("department").getFirst());
    }

    @Test
    void givenCreateUserCommand_WhenToUserRepresentation_ThenKeepConfiguredValues() {
        var command = new CreateIdentityUserCommand(
                "mary",
                "mary@example.com",
                "Mary",
                "Stone",
                true,
                true,
                Map.of("profile", List.of("admin"))
        );

        var userRepresentation = this.mapper.toUserRepresentation(command);

        assertEquals("mary", userRepresentation.getUsername());
        assertEquals("mary@example.com", userRepresentation.getEmail());
        assertTrue(userRepresentation.isEmailVerified());
        assertEquals("admin", userRepresentation.getAttributes().get("profile").getFirst());
    }

    @Test
    void givenGroupRepresentation_WhenToIdentityGroup_ThenMapPathAndAttributes() {
        var groupRepresentation = new GroupRepresentation();
        groupRepresentation.setId("group-1");
        groupRepresentation.setName("Backoffice");
        groupRepresentation.setPath("/Backoffice");
        groupRepresentation.setAttributes(Map.of("state", List.of("RJ")));

        var identityGroup = this.mapper.toIdentityGroup(groupRepresentation);

        assertEquals("group-1", identityGroup.id());
        assertEquals("/Backoffice", identityGroup.path());
        assertEquals("RJ", identityGroup.attributes().get("state").getFirst());
    }
}
