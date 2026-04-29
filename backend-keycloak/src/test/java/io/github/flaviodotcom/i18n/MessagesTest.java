package io.github.flaviodotcom.i18n;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessagesTest {

    @Test
    void givenDefaultLocale_WhenGet_ThenReturnEnglishMessage() {
        var message = Messages.get(Locale.ENGLISH, "error.query-param.blank", "enabled");

        assertEquals("Query param 'enabled' cannot be blank.", message);
    }

    @Test
    void givenBrazilianLocale_WhenGet_ThenReturnPortugueseMessage() {
        var message = Messages.get(Locale.forLanguageTag("pt-BR"), "error.query-param.blank", "enabled");

        assertEquals("Parâmetro de consulta 'enabled' não pode ficar em branco.", message);
    }

    @Test
    void givenPortugueseJvmDefaultLocale_WhenGetEnglishMessage_ThenDoNotUseJvmLocaleFallback() {
        var previousDefaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("pt-BR"));
        ResourceBundle.clearCache();

        try {
            var message = Messages.get(Locale.ENGLISH, "problem.validation.title");

            assertEquals("Invalid request data", message);
        } finally {
            Locale.setDefault(previousDefaultLocale);
            ResourceBundle.clearCache();
        }
    }
}
