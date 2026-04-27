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
    void givenGenericConflictDuringUserCreation_WhenTranslate_ThenReturnUsernameOrEmailConflictMessage() {
        var translated = KeycloakErrorTranslator.translate(409, "Conflict", KeycloakErrorContext.USER_CREATION);

        assertEquals("A user already exists with the provided username or email.", translated.detail());
        assertEquals("keycloak.error.user-conflict.username-or-email", translated.messageKey());
    }

    @Test
    void givenGenericConflictWithoutContext_WhenTranslate_ThenKeepOriginalDetail() {
        var translated = KeycloakErrorTranslator.translate(409, "Conflict");

        assertEquals("Conflict", translated.detail());
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
    void givenUpdatePasswordEmailFailure_WhenTranslate_ThenReturnFriendlyMessage() {
        var translated = KeycloakErrorTranslator.translate(500, "Internal Server Error", KeycloakErrorContext.UPDATE_PASSWORD_EMAIL);

        assertEquals("Could not send the update password email. Check the SMTP configuration.", translated.detail());
        assertEquals("keycloak.error.update-password-email.unavailable", translated.messageKey());
    }

    @Test
    void givenInternalServerErrorWithoutContext_WhenTranslate_ThenKeepOriginalDetail() {
        var translated = KeycloakErrorTranslator.translate(500, "Internal Server Error");

        assertEquals("Internal Server Error", translated.detail());
    }

    @Test
    void givenUnknownError_WhenTranslate_ThenKeepOriginalDetail() {
        var detail = "{\"error\":\"unknown_error\"}";

        var translated = KeycloakErrorTranslator.translate(400, detail);

        assertEquals(detail, translated.detail());
    }
}
