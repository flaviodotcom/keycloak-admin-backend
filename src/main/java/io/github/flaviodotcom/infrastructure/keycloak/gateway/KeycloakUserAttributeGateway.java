package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityUserAttributeCommand;
import io.github.flaviodotcom.domain.identity.gateway.IdentityUserAttributeGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityUserAttribute;
import io.github.flaviodotcom.domain.shared.SearchableAttributeName;
import io.github.flaviodotcom.infrastructure.keycloak.mapper.KeycloakUserProfileAttributeMapper;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakHttpResponseHandler;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakUserAttributeDefinitionResolver;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakUserAttributeLocalization;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.AllArgsConstructor;
import org.keycloak.representations.userprofile.config.UPConfig;

import java.util.ArrayList;
import java.util.List;

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
            config.setAttributes(new ArrayList<>(config.getAttributes() == null ? List.of() : config.getAttributes()));
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
    public IdentityUserAttribute findAttribute(String name) {
        try {
            var config = this.keycloak.userProfile().getConfiguration();
            return this.definitionResolver.resolve(config, name);
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

    private void removeCreatedAttribute(UPConfig config,
                                        CreateIdentityUserAttributeCommand command,
                                        RuntimeException creationFailure) {
        try {
            var createdAttributeNames = this.createdAttributeNames(command);
            config.setAttributes(config.getAttributes().stream()
                    .filter(attribute -> !createdAttributeNames.contains(attribute.getName()))
                    .toList());
            this.keycloak.userProfile().update(config);
        } catch (RuntimeException compensationFailure) {
            creationFailure.addSuppressed(compensationFailure);
        }
    }

    private List<String> createdAttributeNames(CreateIdentityUserAttributeCommand command) {
        if (Boolean.TRUE.equals(command.insensitive())) {
            return List.of(command.name(), SearchableAttributeName.toInternalName(command.name()));
        }

        return List.of(command.name());
    }
}
