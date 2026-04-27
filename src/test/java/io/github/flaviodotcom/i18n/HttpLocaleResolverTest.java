package io.github.flaviodotcom.i18n;

import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpLocaleResolverTest {

    @Test
    void givenMissingHeaders_WhenResolve_ThenReturnEnglish() {
        var locale = HttpLocaleResolver.resolve((HttpHeaders) null);

        assertEquals(Locale.ENGLISH, locale);
    }

    @Test
    void givenWildcardLanguage_WhenResolve_ThenReturnEnglish() {
        var headers = mock(HttpHeaders.class);

        when(headers.getHeaderString(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("*");

        var locale = HttpLocaleResolver.resolve(headers);

        assertEquals(Locale.ENGLISH, locale);
    }

    @Test
    void givenWildcardBeforeExplicitLanguage_WhenResolve_ThenReturnFirstExplicitLanguage() {
        var headers = mock(HttpHeaders.class);
        var portuguese = Locale.forLanguageTag("pt-BR");

        when(headers.getHeaderString(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("*, pt-BR");

        var locale = HttpLocaleResolver.resolve(headers);

        assertEquals(portuguese, locale);
    }

    @Test
    void givenEnglishLanguage_WhenResolve_ThenReturnEnglish() {
        var headers = mock(HttpHeaders.class);

        when(headers.getHeaderString(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("en");

        var locale = HttpLocaleResolver.resolve(headers);

        assertEquals(Locale.ENGLISH, locale);
    }

    @Test
    void givenUnsupportedLanguage_WhenResolve_ThenReturnEnglish() {
        var headers = mock(HttpHeaders.class);

        when(headers.getHeaderString(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("es");

        var locale = HttpLocaleResolver.resolve(headers);

        assertEquals(Locale.ENGLISH, locale);
    }
}
