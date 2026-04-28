package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityUserAttributeCommand;
import io.github.flaviodotcom.domain.identity.command.UpdateIdentityUserAttributeCommand;
import io.github.flaviodotcom.domain.identity.gateway.IdentityUserAttributeGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityUserAttribute;
import io.github.flaviodotcom.domain.shared.SearchableAttributeName;
import io.github.flaviodotcom.infrastructure.keycloak.mapper.KeycloakUserProfileAttributeMapper;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakHttpResponseHandler;
import io.github.flaviodotcom.infrastructure.keycloak.userprofile.KeycloakUserAttributeDefinitionResolver;
import io.github.flaviodotcom.infrastructure.keycloak.userprofile.KeycloakUserAttributeLocalization;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.AllArgsConstructor;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.keycloak.representations.userprofile.config.UPAttribute;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakUserAttributeGateway implements IdentityUserAttributeGateway {

    private final KeycloakAdminSupport keycloak;
    private final KeycloakUserProfileAttributeMapper mapper;
    private final KeycloakUserAttributeDefinitionResolver definitionResolver;
    private final KeycloakUserAttributeLocalization localization;

    @Override
    public IdentityUserAttribute createAttribute(CreateIdentityUserAttributeCommand command) {
        try {
            var config = this.keycloak.userProfile().getConfiguration();
            this.ensureMutableAttributes(config);
            this.definitionResolver.ensureCanCreate(config, command.name());
            config.addOrReplaceAttribute(this.mapper.toPublicAttribute(command));
            if (Boolean.TRUE.equals(command.insensitive())) {
                config.addOrReplaceAttribute(this.mapper.toInternalAttribute(command));
            }

            this.keycloak.userProfile().update(config);
            this.saveLocalizationOrRemoveAttribute(config, command);
            return this.mapper.toIdentityUserAttribute(command);
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    @Override
    public IdentityUserAttribute updateAttribute(UpdateIdentityUserAttributeCommand command) {
        try {
            var config = this.keycloak.userProfile().getConfiguration();
            this.ensureMutableAttributes(config);
            this.definitionResolver.resolve(config, command.name());
            var originalAttributes = List.copyOf(config.getAttributes());

            config.addOrReplaceAttribute(this.mapper.toPublicAttribute(command));
            if (Boolean.TRUE.equals(command.insensitive())) {
                config.addOrReplaceAttribute(this.mapper.toInternalAttribute(command));
            } else {
                this.removeAttributes(config, SearchableAttributeName.toInternalName(command.name()));
            }

            this.keycloak.userProfile().update(config);
            this.saveLocalizationOrRestoreAttributes(config, originalAttributes, command);
            return this.mapper.toIdentityUserAttribute(command);
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    @Override
    public void deleteAttribute(String name) {
        try {
            SearchableAttributeName.requirePublicName(name);
            var config = this.keycloak.userProfile().getConfiguration();
            this.ensureMutableAttributes(config);
            this.definitionResolver.resolve(config, name);
            this.removeAttributes(config, name, SearchableAttributeName.toInternalName(name));
            this.keycloak.userProfile().update(config);
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    @Override
    public IdentityUserAttribute findAttribute(String name) {
        try {
            var config = this.keycloak.userProfile().getConfiguration();
            return this.definitionResolver.resolve(config, name);
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    @Override
    public Map<String, IdentityUserAttribute> findAttributes(Set<String> names) {
        try {
            var config = this.keycloak.userProfile().getConfiguration();
            var attributes = new LinkedHashMap<String, IdentityUserAttribute>();
            for (var name : names) {
                attributes.put(name, this.definitionResolver.resolve(config, name));
            }
            return Map.copyOf(attributes);
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    private void saveLocalizationOrRemoveAttribute(UPConfig config, CreateIdentityUserAttributeCommand command) {
        try {
            this.localization.saveDisplayName(command.name(), command.displayName());
        } catch (RuntimeException exception) {
            this.removeCreatedAttribute(config, command, exception);
            throw exception;
        }
    }

    private void saveLocalizationOrRestoreAttributes(UPConfig config,
                                                     List<UPAttribute> originalAttributes,
                                                     UpdateIdentityUserAttributeCommand command) {
        try {
            this.localization.saveDisplayName(command.name(), command.displayName());
        } catch (RuntimeException exception) {
            this.restoreAttributes(config, originalAttributes, exception);
            throw exception;
        }
    }

    private void removeCreatedAttribute(UPConfig config,
                                        CreateIdentityUserAttributeCommand command,
                                        RuntimeException creationFailure) {
        try {
            var createdAttributeNames = this.createdAttributeNames(command);
            this.removeAttributes(config, createdAttributeNames.toArray(String[]::new));
            this.keycloak.userProfile().update(config);
        } catch (RuntimeException compensationFailure) {
            creationFailure.addSuppressed(compensationFailure);
        }
    }

    private void restoreAttributes(UPConfig config,
                                   List<UPAttribute> originalAttributes,
                                   RuntimeException updateFailure) {
        try {
            config.setAttributes(new ArrayList<>(originalAttributes));
            this.keycloak.userProfile().update(config);
        } catch (RuntimeException compensationFailure) {
            updateFailure.addSuppressed(compensationFailure);
        }
    }

    private void removeAttributes(UPConfig config, String... names) {
        var attributeNames = Set.of(names);
        config.setAttributes(config.getAttributes().stream()
                .filter(attribute -> !attributeNames.contains(attribute.getName()))
                .toList());
    }

    private void ensureMutableAttributes(UPConfig config) {
        config.setAttributes(new ArrayList<>(config.getAttributes() == null ? List.of() : config.getAttributes()));
    }

    private List<String> createdAttributeNames(CreateIdentityUserAttributeCommand command) {
        if (Boolean.TRUE.equals(command.insensitive())) {
            return List.of(command.name(), SearchableAttributeName.toInternalName(command.name()));
        }

        return List.of(command.name());
    }
}
