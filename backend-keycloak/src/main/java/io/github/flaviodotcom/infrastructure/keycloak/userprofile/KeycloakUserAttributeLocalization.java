package io.github.flaviodotcom.infrastructure.keycloak.userprofile;

import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

import java.util.Map;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakUserAttributeLocalization {

    private static final String DISPLAY_NAME_KEY_PATTERN = "identity.user.attribute.%s.displayName";

    private final KeycloakAdminSupport keycloak;

    public void saveDisplayName(String attributeName, Map<String, String> displayName) {
        var localizationKey = this.toDisplayNameKey(attributeName);
        for (var translation : displayName.entrySet()) {
            this.keycloak.realm().localization().createOrUpdateRealmLocalizationTexts(
                    translation.getKey(),
                    Map.of(localizationKey, translation.getValue())
            );
        }
    }

    public String toDisplayNameReference(String attributeName) {
        return "${%s}".formatted(this.toDisplayNameKey(attributeName));
    }

    public String toDisplayNameKey(String attributeName) {
        return DISPLAY_NAME_KEY_PATTERN.formatted(attributeName);
    }
}
