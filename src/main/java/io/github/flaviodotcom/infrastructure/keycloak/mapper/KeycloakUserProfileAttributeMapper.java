package io.github.flaviodotcom.infrastructure.keycloak.mapper;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityUserAttributeCommand;
import io.github.flaviodotcom.domain.identity.command.UpdateIdentityUserAttributeCommand;
import io.github.flaviodotcom.domain.identity.model.IdentityUserAttribute;
import io.github.flaviodotcom.domain.shared.SearchableAttributeName;
import io.github.flaviodotcom.exceptions.BusinessException;
import io.github.flaviodotcom.infrastructure.keycloak.userprofile.KeycloakUserAttributeLocalization;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import org.keycloak.representations.userprofile.config.UPAttribute;
import org.keycloak.representations.userprofile.config.UPAttributePermissions;
import org.keycloak.representations.userprofile.config.UPAttributeRequired;

import java.util.Map;
import java.util.Set;

import static io.github.flaviodotcom.infrastructure.keycloak.userprofile.KeycloakUserAttributeMetadata.ADMIN_ONLY_ROLE;
import static io.github.flaviodotcom.infrastructure.keycloak.userprofile.KeycloakUserAttributeMetadata.ADMIN_USER_ROLES;
import static io.github.flaviodotcom.infrastructure.keycloak.userprofile.KeycloakUserAttributeMetadata.INSENSITIVE_ANNOTATION;
import static io.github.flaviodotcom.infrastructure.keycloak.userprofile.KeycloakUserAttributeMetadata.INTERNAL_ANNOTATION;
import static io.github.flaviodotcom.infrastructure.keycloak.userprofile.KeycloakUserAttributeMetadata.SOURCE_ATTRIBUTE_ANNOTATION;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakUserProfileAttributeMapper {

    private final KeycloakUserAttributeLocalization localization;

    public UPAttribute toPublicAttribute(CreateIdentityUserAttributeCommand command) {
        return this.toPublicAttribute(
                command.name(),
                command.insensitive(),
                command.required(),
                command.multivalued()
        );
    }

    public UPAttribute toPublicAttribute(UpdateIdentityUserAttributeCommand command) {
        return this.toPublicAttribute(
                command.name(),
                command.insensitive(),
                command.required(),
                command.multivalued()
        );
    }

    private UPAttribute toPublicAttribute(String name, Boolean insensitive, Boolean required, Boolean multivalued) {
        var attribute = new UPAttribute(name, this.permissions(ADMIN_USER_ROLES, ADMIN_USER_ROLES));
        attribute.setDisplayName(this.localization.toDisplayNameReference(name));
        attribute.setMultivalued(multivalued);
        if (Boolean.TRUE.equals(required)) {
            attribute.setRequired(new UPAttributeRequired());
        }
        attribute.setAnnotations(Map.of(INSENSITIVE_ANNOTATION, insensitive));
        return attribute;
    }

    public UPAttribute toInternalAttribute(CreateIdentityUserAttributeCommand command) {
        return this.toInternalAttribute(command.name(), command.multivalued());
    }

    public UPAttribute toInternalAttribute(UpdateIdentityUserAttributeCommand command) {
        return this.toInternalAttribute(command.name(), command.multivalued());
    }

    private UPAttribute toInternalAttribute(String name, Boolean multivalued) {
        var internalName = SearchableAttributeName.toInternalName(name);
        var attribute = new UPAttribute(internalName, this.permissions(ADMIN_ONLY_ROLE, ADMIN_ONLY_ROLE));
        attribute.setMultivalued(multivalued);
        attribute.setAnnotations(Map.of(
                INSENSITIVE_ANNOTATION, Boolean.TRUE,
                INTERNAL_ANNOTATION, Boolean.TRUE,
                SOURCE_ATTRIBUTE_ANNOTATION, name
        ));
        return attribute;
    }

    public IdentityUserAttribute toIdentityUserAttribute(CreateIdentityUserAttributeCommand command) {
        return this.toIdentityUserAttribute(
                command.name(),
                command.displayName(),
                command.insensitive(),
                command.required(),
                command.multivalued()
        );
    }

    public IdentityUserAttribute toIdentityUserAttribute(UpdateIdentityUserAttributeCommand command) {
        return this.toIdentityUserAttribute(
                command.name(),
                command.displayName(),
                command.insensitive(),
                command.required(),
                command.multivalued()
        );
    }

    private IdentityUserAttribute toIdentityUserAttribute(String name,
                                                         Map<String, String> displayName,
                                                         Boolean insensitive,
                                                         Boolean required,
                                                         Boolean multivalued) {
        return new IdentityUserAttribute(
                name,
                displayName,
                insensitive,
                required,
                multivalued
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
