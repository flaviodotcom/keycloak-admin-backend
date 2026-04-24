package io.github.flaviodotcom.infrastructure.keycloak.support;

import io.github.flaviodotcom.domain.identity.model.IdentityUserAttribute;
import io.github.flaviodotcom.domain.shared.SearchableAttributeName;
import io.github.flaviodotcom.exceptions.BusinessException;
import io.github.flaviodotcom.infrastructure.keycloak.mapper.KeycloakUserProfileAttributeMapper;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import org.keycloak.representations.userprofile.config.UPConfig;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakUserAttributeDefinitionResolver {

    private final KeycloakUserProfileAttributeMapper mapper;

    public void ensureCanCreate(UPConfig config, String attributeName) {
        SearchableAttributeName.requirePublicName(attributeName);
        this.ensureAttributeDoesNotExist(config, attributeName);
        this.ensureAttributeDoesNotExist(config, SearchableAttributeName.toInternalName(attributeName));
    }

    public IdentityUserAttribute resolve(UPConfig config, String attributeName) {
        SearchableAttributeName.requirePublicName(attributeName);
        var attribute = config.getAttribute(attributeName);
        if (attribute == null) {
            throw new BusinessException("User attribute '%s' is not configured.".formatted(attributeName));
        }

        return this.mapper.toIdentityUserAttribute(attribute);
    }

    private void ensureAttributeDoesNotExist(UPConfig config, String attributeName) {
        if (config.getAttribute(attributeName) != null) {
            throw new BusinessException("User attribute '%s' is already configured.".formatted(attributeName));
        }
    }
}
