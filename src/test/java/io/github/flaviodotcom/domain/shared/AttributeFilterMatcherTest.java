package io.github.flaviodotcom.domain.shared;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttributeFilterMatcherTest {

    @Test
    void givenRequestedAttributes_WhenMatches_ThenRequireEveryAttribute() {
        var requestedAttributes = Map.of(
                "department", "rh",
                "state", "rj"
        );
        var currentAttributes = Map.of(
                "department", List.of("RH"),
                "state", List.of("RJ")
        );

        assertTrue(AttributeFilterMatcher.matches(requestedAttributes, currentAttributes, true));
    }

    @Test
    void givenMissingAttribute_WhenMatches_ThenReturnFalse() {
        var requestedAttributes = Map.of("department", "IT");
        var currentAttributes = Map.of("state", List.of("RJ"));

        assertFalse(AttributeFilterMatcher.matches(requestedAttributes, currentAttributes, true));
    }
}
