package io.github.flaviodotcom.infrastructure.keycloak.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeycloakErrorTranslatorTest {

    @Test
    void givenUsernameOrEmailConflict_WhenTranslate_ThenReturnFriendlyMessage() {
        var detail = "{\"errorMessage\":\"User exists with same username or email\"}";

        var translated = KeycloakErrorTranslator.translate(409, detail);

        assertEquals("A user already exists with the provided username or email.", translated.detail());
        assertEquals("keycloak.error.user-conflict.username-or-email", translated.messageKey());
    }

    @Test
    void givenUsernameConflict_WhenTranslate_ThenReturnFriendlyMessage() {
        var detail = "{\"errorMessage\":\"User exists with same username\"}";

        var translated = KeycloakErrorTranslator.translate(409, detail);

        assertEquals("A user already exists with the provided username.", translated.detail());
        assertEquals("keycloak.error.user-conflict.username", translated.messageKey());
    }

    @Test
    void givenEmailConflict_WhenTranslate_ThenReturnFriendlyMessage() {
        var detail = "{\"errorMessage\":\"User exists with same email\"}";

        var translated = KeycloakErrorTranslator.translate(409, detail);

        assertEquals("A user already exists with the provided email.", translated.detail());
        assertEquals("keycloak.error.user-conflict.email", translated.messageKey());
    }

    @Test
    void givenRequiredAttributeError_WhenTranslate_ThenReturnFriendlyMessageWithAttributeName() {
        var detail = """
                {
                  "errors": [
                    {
                      "field": "attributes.cpf",
                      "errorMessage": "error-user-attribute-required"
                    }
                  ]
                }
                """;

        var translated = KeycloakErrorTranslator.translate(400, detail);

        assertEquals("Required user attribute 'cpf' was not provided.", translated.detail());
        assertEquals("keycloak.error.user-attribute.required.named", translated.messageKey());
    }

    @Test
    void givenUnknownError_WhenTranslate_ThenKeepOriginalDetail() {
        var detail = "{\"error\":\"unknown_error\"}";

        var translated = KeycloakErrorTranslator.translate(400, detail);

        assertEquals(detail, translated.detail());
    }
}
