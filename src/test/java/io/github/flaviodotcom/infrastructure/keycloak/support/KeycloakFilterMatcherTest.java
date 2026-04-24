package io.github.flaviodotcom.infrastructure.keycloak.support;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakFilterMatcherTest {

    @Test
    void givenPartialTextFilter_WhenMatchesText_ThenIgnoreCaseAndReturnTrue() {
        assertTrue(KeycloakFilterMatcher.matchesText("adm", "Realm Admin", false));
    }

    @Test
    void givenExactTextFilter_WhenMatchesText_ThenRequireSameNormalizedValue() {
        assertTrue(KeycloakFilterMatcher.matchesText("realm admin", "Realm Admin", true));
        assertFalse(KeycloakFilterMatcher.matchesText("admin", "Realm Admin", true));
    }

    @Test
    void givenRequestedAttributes_WhenMatchesAttributes_ThenRequireEveryAttribute() {
        var requestedAttributes = Map.of(
                "department", "it",
                "state", "rj"
        );
        var currentAttributes = Map.of(
                "department", List.of("IT"),
                "state", List.of("RJ")
        );

        assertTrue(KeycloakFilterMatcher.matchesAttributes(requestedAttributes, currentAttributes, true));
    }

    @Test
    void givenMissingAttribute_WhenMatchesAttributes_ThenReturnFalse() {
        var requestedAttributes = Map.of("department", "IT");
        var currentAttributes = Map.of("state", List.of("RJ"));

        assertFalse(KeycloakFilterMatcher.matchesAttributes(requestedAttributes, currentAttributes, true));
    }
}
