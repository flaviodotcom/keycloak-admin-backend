package io.github.flaviodotcom.domain.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextFilterMatcherTest {

    @Test
    void givenPartialTextFilter_WhenMatches_ThenIgnoreCaseAndAccents() {
        assertTrue(TextFilterMatcher.matches("joao", "Joao da Silva", false));
        assertTrue(TextFilterMatcher.matches("joao", "João da Silva", false));
        assertTrue(TextFilterMatcher.matches("É", "Jose", false));
    }

    @Test
    void givenExactTextFilter_WhenMatches_ThenCompareNormalizedValues() {
        assertTrue(TextFilterMatcher.matches("maria jose", "Maria José", true));
        assertFalse(TextFilterMatcher.matches("maria", "Maria José", true));
    }
}
