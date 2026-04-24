package io.github.flaviodotcom.domain.shared;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchTermBuilderTest {

    @Test
    void givenTextWithAccentsAndSpaces_WhenBuild_ThenReturnOriginalNormalizedAndTokens() {
        assertEquals(
                List.of("Conceição Teste", "conceicao teste", "Conceição", "conceicao", "Teste", "teste"),
                SearchTermBuilder.build("Conceição Teste")
        );
    }
}
