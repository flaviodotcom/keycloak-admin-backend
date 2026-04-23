package io.github.flaviodotcom.infrastructure.keycloak;

import io.github.flaviodotcom.domain.identity.CreateIdentityGroupCommand;
import io.github.flaviodotcom.domain.identity.CreateIdentityRoleCommand;
import io.github.flaviodotcom.domain.identity.CreateIdentityUserCommand;
import io.github.flaviodotcom.domain.identity.IdentityGroup;
import io.github.flaviodotcom.domain.identity.IdentityRole;
import io.github.flaviodotcom.domain.identity.IdentityUser;
import jakarta.enterprise.context.ApplicationScoped;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ApplicationScoped
public class KeycloakRepresentationMapper {

    public IdentityUser toIdentityUser(UserRepresentation representation) {
        return new IdentityUser(
                representation.getId(),
                representation.getUsername(),
                representation.getEmail(),
                representation.getFirstName(),
                representation.getLastName(),
                representation.isEnabled(),
                representation.isEmailVerified(),
                representation.getCreatedTimestamp(),
                this.copyAttributes(representation.getAttributes())
        );
    }

    public IdentityGroup toIdentityGroup(GroupRepresentation representation) {
        return new IdentityGroup(
                representation.getId(),
                representation.getName(),
                representation.getPath(),
                this.copyAttributes(representation.getAttributes())
        );
    }

    public IdentityRole toIdentityRole(RoleRepresentation representation) {
        return new IdentityRole(
                representation.getId(),
                representation.getName(),
                representation.getDescription(),
                representation.isComposite(),
                representation.getClientRole(),
                representation.getContainerId()
        );
    }

    public UserRepresentation toUserRepresentation(CreateIdentityUserCommand command) {
        var representation = new UserRepresentation();
        representation.setUsername(command.username());
        representation.setEmail(command.email());
        representation.setFirstName(command.firstName());
        representation.setLastName(command.lastName());
        representation.setEnabled(command.enabled());
        representation.setEmailVerified(command.emailVerified());
        representation.setAttributes(this.copyAttributes(command.attributes()));
        return representation;
    }

    public GroupRepresentation toGroupRepresentation(CreateIdentityGroupCommand command) {
        var representation = new GroupRepresentation();
        representation.setName(command.name());
        representation.setAttributes(this.copyAttributes(command.attributes()));
        return representation;
    }

    public RoleRepresentation toRoleRepresentation(CreateIdentityRoleCommand command) {
        var representation = new RoleRepresentation();
        representation.setName(command.name());
        representation.setDescription(command.description());
        return representation;
    }

    private Map<String, List<String>> copyAttributes(Map<String, List<String>> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Map.of();
        }

        var copiedAttributes = new LinkedHashMap<String, List<String>>();
        for (var attribute : attributes.entrySet()) {
            var key = Objects.requireNonNull(attribute.getKey(), "Attribute key is required.");
            var values = List.copyOf(Objects.requireNonNull(attribute.getValue(), "Attribute values are required."));
            copiedAttributes.put(key, values);
        }
        return Map.copyOf(copiedAttributes);
    }
}
