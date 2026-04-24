package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityUserAttributeCommand;
import io.github.flaviodotcom.exceptions.BusinessException;
import io.github.flaviodotcom.infrastructure.keycloak.mapper.KeycloakUserProfileAttributeMapper;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakUserAttributeDefinitionResolver;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakUserAttributeLocalization;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.RealmLocalizationResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserProfileResource;
import org.keycloak.representations.userprofile.config.UPAttribute;
import org.keycloak.representations.userprofile.config.UPConfig;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KeycloakUserAttributeGatewayTest {

    @Test
    void givenInsensitiveAttribute_WhenCreateAttribute_ThenCreatePublicAndInternalAttributes() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var realm = mock(RealmResource.class);
        var localization = mock(RealmLocalizationResource.class);
        var userProfile = mock(UserProfileResource.class);
        var config = new UPConfig();
        config.setAttributes(new ArrayList<>());
        var gateway = newGateway(keycloak);

        when(keycloak.realm()).thenReturn(realm);
        when(realm.localization()).thenReturn(localization);
        when(keycloak.userProfile()).thenReturn(userProfile);
        when(userProfile.getConfiguration()).thenReturn(config);

        var attribute = gateway.createAttribute(new CreateIdentityUserAttributeCommand(
                "departamento",
                Map.of("pt-BR", "Departamento", "en", "Department"),
                true,
                true,
                true
        ));

        assertEquals("departamento", attribute.name());
        assertEquals("Departamento", attribute.displayName().get("pt-BR"));
        assertEquals(true, attribute.insensitive());
        assertEquals(true, attribute.required());
        assertEquals(true, attribute.multivalued());
        assertEquals("departamento", config.getAttributes().getFirst().getName());
        assertEquals("${identity.user.attribute.departamento.displayName}", config.getAttributes().getFirst().getDisplayName());
        assertTrue(config.getAttributes().get(0).getRequired().isAlways());
        assertTrue(config.getAttributes().get(0).isMultivalued());
        assertEquals("__search_departamento", config.getAttributes().get(1).getName());
        assertTrue(config.getAttributes().get(1).isMultivalued());
        verify(userProfile).update(config);
        verify(localization).createOrUpdateRealmLocalizationTexts(
                "pt-BR",
                Map.of("identity.user.attribute.departamento.displayName", "Departamento")
        );
        verify(localization).createOrUpdateRealmLocalizationTexts(
                "en",
                Map.of("identity.user.attribute.departamento.displayName", "Department")
        );
    }

    @Test
    void givenUnmanagedAttribute_WhenFindAttribute_ThenReturnLegacySensitiveAttribute() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var userProfile = mock(UserProfileResource.class);
        var config = new UPConfig();
        config.setAttributes(new ArrayList<>());
        config.addOrReplaceAttribute(new UPAttribute("cpf"));
        var gateway = newGateway(keycloak);

        when(keycloak.userProfile()).thenReturn(userProfile);
        when(userProfile.getConfiguration()).thenReturn(config);

        var attribute = gateway.findAttribute("cpf");

        assertEquals("cpf", attribute.name());
        assertEquals(false, attribute.insensitive());
        assertEquals(false, attribute.required());
        assertEquals(false, attribute.multivalued());
    }

    @Test
    void givenUnknownAttribute_WhenFindAttribute_ThenThrowBusinessException() {
        var keycloak = mock(KeycloakAdminSupport.class);
        var userProfile = mock(UserProfileResource.class);
        var config = new UPConfig();
        config.setAttributes(new ArrayList<>());
        var gateway = newGateway(keycloak);

        when(keycloak.userProfile()).thenReturn(userProfile);
        when(userProfile.getConfiguration()).thenReturn(config);

        assertThrows(BusinessException.class, () -> gateway.findAttribute("cpf"));
    }

    private static KeycloakUserAttributeGateway newGateway(KeycloakAdminSupport keycloak) {
        var localization = new KeycloakUserAttributeLocalization(keycloak);
        var mapper = new KeycloakUserProfileAttributeMapper(localization);
        var definitionResolver = new KeycloakUserAttributeDefinitionResolver(mapper);
        return new KeycloakUserAttributeGateway(keycloak, mapper, definitionResolver, localization);
    }
}
