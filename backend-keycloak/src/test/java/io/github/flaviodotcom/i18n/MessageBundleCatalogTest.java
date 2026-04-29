package io.github.flaviodotcom.i18n;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageBundleCatalogTest {

    @Test
    void givenDefaultLocale_WhenCheckSupport_ThenReturnSupported() {
        assertTrue(MessageBundleCatalog.isSupported(Locale.ENGLISH));
    }

    @Test
    void givenLocaleWithMessagesAndValidationMessages_WhenCheckSupport_ThenReturnSupported() {
        assertTrue(MessageBundleCatalog.isSupported(Locale.forLanguageTag("pt-BR")));
    }

    @Test
    void givenLocaleWithoutBundles_WhenCheckSupport_ThenReturnUnsupported() {
        assertFalse(MessageBundleCatalog.isSupported(Locale.forLanguageTag("es")));
    }

    @Test
    void givenUnsupportedLocale_WhenResolveSupported_ThenReturnDefaultLocale() {
        var locale = MessageBundleCatalog.supportedOrDefault(Locale.forLanguageTag("es"));

        assertEquals(Locale.ENGLISH, locale);
    }
}
