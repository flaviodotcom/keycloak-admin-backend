package io.github.flaviodotcom.infrastructure.keycloak.mapper;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityGroupCommand;
import io.github.flaviodotcom.domain.identity.command.CreateIdentityRoleCommand;
import io.github.flaviodotcom.domain.identity.command.CreateIdentityUserCommand;
import io.github.flaviodotcom.domain.identity.command.UpdateIdentityGroupCommand;
import io.github.flaviodotcom.domain.identity.command.UpdateIdentityRoleCommand;
import io.github.flaviodotcom.domain.identity.command.UpdateIdentityUserCommand;
import io.github.flaviodotcom.domain.identity.model.IdentityGroup;
import io.github.flaviodotcom.domain.identity.model.IdentityRole;
import io.github.flaviodotcom.domain.identity.model.IdentityUser;
import io.github.flaviodotcom.domain.shared.SearchableAttributeName;
import io.github.flaviodotcom.i18n.Messages;
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
                this.copyAttributes(representation.getAttributes(), false)
        );
    }

    public IdentityGroup toIdentityGroup(GroupRepresentation representation) {
        return new IdentityGroup(
                representation.getId(),
                representation.getName(),
                representation.getPath(),
                this.copyAttributes(representation.getAttributes(), false)
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
        representation.setAttributes(this.copyAttributes(command.attributes(), true));
        return representation;
    }

    public UserRepresentation toUserRepresentation(String id, UpdateIdentityUserCommand command) {
        var representation = new UserRepresentation();
        representation.setId(id);
        representation.setUsername(command.username());
        representation.setEmail(command.email());
        representation.setFirstName(command.firstName());
        representation.setLastName(command.lastName());
        representation.setEnabled(command.enabled());
        representation.setEmailVerified(command.emailVerified());
        representation.setAttributes(this.copyAttributes(command.attributes(), true));
        return representation;
    }

    public GroupRepresentation toGroupRepresentation(CreateIdentityGroupCommand command) {
        var representation = new GroupRepresentation();
        representation.setName(command.name());
        representation.setAttributes(this.copyAttributes(command.attributes(), false));
        return representation;
    }

    public GroupRepresentation toGroupRepresentation(String id, UpdateIdentityGroupCommand command) {
        var representation = new GroupRepresentation();
        representation.setId(id);
        representation.setName(command.name());
        representation.setAttributes(this.copyAttributes(command.attributes(), false));
        return representation;
    }

    public RoleRepresentation toRoleRepresentation(CreateIdentityRoleCommand command) {
        var representation = new RoleRepresentation();
        representation.setName(command.name());
        representation.setDescription(command.description());
        return representation;
    }

    public RoleRepresentation toRoleRepresentation(String id, UpdateIdentityRoleCommand command) {
        var representation = new RoleRepresentation();
        representation.setId(id);
        representation.setName(command.name());
        representation.setDescription(command.description());
        return representation;
    }

    private Map<String, List<String>> copyAttributes(Map<String, List<String>> attributes, boolean writable) {
        if (attributes == null || attributes.isEmpty()) {
            return Map.of();
        }

        var copiedAttributes = new LinkedHashMap<String, List<String>>();
        for (var attribute : attributes.entrySet()) {
            var key = Objects.requireNonNull(attribute.getKey(), Messages.getDefault("error.attribute-key.required"));
            if (!writable && SearchableAttributeName.isInternalName(key)) continue;
            var values = List.copyOf(Objects.requireNonNull(attribute.getValue(), Messages.getDefault("error.attribute-values.required")));
            copiedAttributes.put(key, values);
        }
        return Map.copyOf(copiedAttributes);
    }
}
