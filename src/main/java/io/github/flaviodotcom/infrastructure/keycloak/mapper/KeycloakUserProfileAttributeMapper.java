package io.github.flaviodotcom.infrastructure.keycloak.mapper;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityUserAttributeCommand;
import io.github.flaviodotcom.domain.identity.model.IdentityUserAttribute;
import io.github.flaviodotcom.domain.shared.SearchableAttributeName;
import io.github.flaviodotcom.exceptions.BusinessException;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakUserAttributeLocalization;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import org.keycloak.representations.userprofile.config.UPAttribute;
import org.keycloak.representations.userprofile.config.UPAttributePermissions;
import org.keycloak.representations.userprofile.config.UPAttributeRequired;

import java.util.Map;
import java.util.Set;

import static io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakUserAttributeMetadata.ADMIN_ONLY_ROLE;
import static io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakUserAttributeMetadata.ADMIN_USER_ROLES;
import static io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakUserAttributeMetadata.INSENSITIVE_ANNOTATION;
import static io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakUserAttributeMetadata.INTERNAL_ANNOTATION;
import static io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakUserAttributeMetadata.SOURCE_ATTRIBUTE_ANNOTATION;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakUserProfileAttributeMapper {

    private final KeycloakUserAttributeLocalization localization;

    public UPAttribute toPublicAttribute(CreateIdentityUserAttributeCommand command) {
        var attribute = new UPAttribute(command.name(), this.permissions(ADMIN_USER_ROLES, ADMIN_USER_ROLES));
        attribute.setDisplayName(this.localization.toDisplayNameReference(command.name()));
        attribute.setMultivalued(command.multivalued());
        if (Boolean.TRUE.equals(command.required())) {
            attribute.setRequired(new UPAttributeRequired());
        }
        attribute.setAnnotations(Map.of(INSENSITIVE_ANNOTATION, command.insensitive()));
        return attribute;
    }

    public UPAttribute toInternalAttribute(CreateIdentityUserAttributeCommand command) {
        var internalName = SearchableAttributeName.toInternalName(command.name());
        var attribute = new UPAttribute(internalName, this.permissions(ADMIN_ONLY_ROLE, ADMIN_ONLY_ROLE));
        attribute.setMultivalued(command.multivalued());
        attribute.setAnnotations(Map.of(
                INSENSITIVE_ANNOTATION, Boolean.TRUE,
                INTERNAL_ANNOTATION, Boolean.TRUE,
                SOURCE_ATTRIBUTE_ANNOTATION, command.name()
        ));
        return attribute;
    }

    public IdentityUserAttribute toIdentityUserAttribute(CreateIdentityUserAttributeCommand command) {
        return new IdentityUserAttribute(
                command.name(),
                command.displayName(),
                command.insensitive(),
                command.required(),
                command.multivalued()
        );
    }

    public IdentityUserAttribute toIdentityUserAttribute(UPAttribute attribute) {
        return new IdentityUserAttribute(
                attribute.getName(),
                Map.of(),
                this.readInsensitive(attribute),
                attribute.getRequired() != null,
                attribute.isMultivalued()
        );
    }

    private UPAttributePermissions permissions(Set<String> viewRoles, Set<String> editRoles) {
        return new UPAttributePermissions(viewRoles, editRoles);
    }

    private Boolean readInsensitive(UPAttribute attribute) {
        var annotations = attribute.getAnnotations();
        if (annotations == null || !annotations.containsKey(INSENSITIVE_ANNOTATION)) {
            return Boolean.FALSE;
        }

        var value = annotations.get(INSENSITIVE_ANNOTATION);
        if (value instanceof Boolean insensitive) {
            return insensitive;
        }

        throw BusinessException.localized("error.user-attribute.invalid-insensitive-metadata", attribute.getName());
    }
}
