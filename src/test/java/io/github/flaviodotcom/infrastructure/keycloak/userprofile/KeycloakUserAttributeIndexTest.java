package io.github.flaviodotcom.infrastructure.keycloak.userprofile;

import io.github.flaviodotcom.domain.identity.gateway.IdentityUserAttributeGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityUserAttribute;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KeycloakUserAttributeIndexTest {

    @Test
    void givenInsensitiveAttribute_WhenIndex_ThenAddNormalizedInternalAttribute() {
        var attributeGateway = mock(IdentityUserAttributeGateway.class);
        var index = new KeycloakUserAttributeIndex(attributeGateway);

        when(attributeGateway.findAttributes(Set.of("departamento")))
                .thenReturn(Map.of("departamento", attribute("departamento", true)));

        var attributes = index.index(Map.of("departamento", List.of("Recursos Humanos")));

        assertEquals(List.of("Recursos Humanos"), attributes.get("departamento"));
        assertEquals(List.of("recursos humanos"), attributes.get("__search_departamento"));
    }

    @Test
    void givenSensitiveAttribute_WhenIndex_ThenKeepOnlyPublicAttribute() {
        var attributeGateway = mock(IdentityUserAttributeGateway.class);
        var index = new KeycloakUserAttributeIndex(attributeGateway);

        when(attributeGateway.findAttributes(Set.of("cpf")))
                .thenReturn(Map.of("cpf", attribute("cpf", false)));

        var attributes = index.index(Map.of("cpf", List.of("12345678901")));

        assertEquals(List.of("12345678901"), attributes.get("cpf"));
        assertFalse(attributes.containsKey("__search_cpf"));
    }

    @Test
    void givenLegacyAttribute_WhenIndex_ThenKeepOnlyPublicAttribute() {
        var attributeGateway = mock(IdentityUserAttributeGateway.class);
        var index = new KeycloakUserAttributeIndex(attributeGateway);

        when(attributeGateway.findAttributes(Set.of("cpf")))
                .thenReturn(Map.of("cpf", attribute("cpf", false)));

        var attributes = index.index(Map.of("cpf", List.of("12345678903")));

        assertEquals(List.of("12345678903"), attributes.get("cpf"));
        assertFalse(attributes.containsKey("__search_cpf"));
    }

    @Test
    void givenInsensitiveAttributeFilter_WhenToSearchAttributes_ThenUseInternalNameAndNormalizedValue() {
        var attributeGateway = mock(IdentityUserAttributeGateway.class);
        var index = new KeycloakUserAttributeIndex(attributeGateway);

        var definitions = new KeycloakUserAttributeDefinitions(Map.of(
                "departamento",
                attribute("departamento", true)
        ));

        var attributes = index.toSearchAttributes(Map.of("departamento", "Recursos Humanos"), definitions);

        assertEquals(Map.of("__search_departamento", "recursos humanos"), attributes);
    }

    @Test
    void givenSensitiveAttribute_WhenMatches_ThenCompareWithoutNormalization() {
        var attributeGateway = mock(IdentityUserAttributeGateway.class);
        var index = new KeycloakUserAttributeIndex(attributeGateway);

        var definitions = new KeycloakUserAttributeDefinitions(Map.of("codigo", attribute("codigo", false)));

        var matched = index.matches(Map.of("codigo", "ABC"), Map.of("codigo", List.of("abc")), false, definitions);

        assertFalse(matched);
    }

    private IdentityUserAttribute attribute(String name, Boolean insensitive) {
        return new IdentityUserAttribute(name, Map.of(), insensitive, false, false);
    }
}
