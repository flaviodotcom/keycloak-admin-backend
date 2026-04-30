package io.github.flaviodotcom.infrastructure.keycloak.support;

import io.github.flaviodotcom.i18n.Messages;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public final class KeycloakErrorTranslator {

    private static final List<Pattern> ATTRIBUTE_NAME_PATTERNS = List.of(
            Pattern.compile("\"field\"\\s*:\\s*\"([^\"]+)\""),
            Pattern.compile("\"attribute\"\\s*:\\s*\"([^\"]+)\"")
    );

    private KeycloakErrorTranslator() {
    }

    public static Result translate(int status, String detail) {
        return translate(status, detail, KeycloakErrorContext.GENERAL);
    }

    public static Result translate(int status, String detail, KeycloakErrorContext context) {
        var normalizedDetail = normalize(detail);
        if (isUserConflict(status, normalizedDetail, context)) {
            return translateUserConflict(normalizedDetail);
        }

        if (isRequiredUserAttribute(status, normalizedDetail)) {
            return translateRequiredUserAttribute(detail);
        }

        if (isUpdatePasswordEmailFailure(status, context)) {
            return Result.localized("keycloak.error.update-password-email.unavailable");
        }

        return Result.original(detail);
    }

    private static boolean isUserConflict(int status, String normalizedDetail, KeycloakErrorContext context) {
        if (status != 409) {
            return false;
        }

        return hasUserConflictDetail(normalizedDetail)
                || context == KeycloakErrorContext.USER_CREATION && isGenericConflict(normalizedDetail);
    }

    private static boolean hasUserConflictDetail(String normalizedDetail) {
        return normalizedDetail.contains("user exists")
                && (normalizedDetail.contains("same username")
                || normalizedDetail.contains("same email")
                || normalizedDetail.contains("same username or email"));
    }

    private static boolean isGenericConflict(String normalizedDetail) {
        return normalizedDetail.isBlank() || "conflict".equals(normalizedDetail);
    }

    private static Result translateUserConflict(String normalizedDetail) {
        if (normalizedDetail.contains("same username or email")) {
            return Result.localized("keycloak.error.user-conflict.username-or-email");
        }

        if (normalizedDetail.contains("same username")) {
            return Result.localized("keycloak.error.user-conflict.username");
        }

        if (normalizedDetail.contains("same email")) {
            return Result.localized("keycloak.error.user-conflict.email");
        }

        return Result.localized("keycloak.error.user-conflict.username-or-email");
    }

    private static boolean isRequiredUserAttribute(int status, String normalizedDetail) {
        return status == 400
                && (normalizedDetail.contains("error-user-attribute-required")
                || normalizedDetail.contains("up-attribute-required")
                || normalizedDetail.contains("user attribute") && normalizedDetail.contains("required"));
    }

    private static Result translateRequiredUserAttribute(String detail) {
        return extractAttributeName(detail)
                .map(attributeName -> Result.localized("keycloak.error.user-attribute.required.named", attributeName))
                .orElse(Result.localized("keycloak.error.user-attribute.required"));
    }

    private static boolean isUpdatePasswordEmailFailure(int status, KeycloakErrorContext context) {
        return status == 500 && context == KeycloakErrorContext.UPDATE_PASSWORD_EMAIL;
    }

    private static Optional<String> extractAttributeName(String detail) {
        if (detail == null || detail.isBlank()) {
            return Optional.empty();
        }

        for (var pattern : ATTRIBUTE_NAME_PATTERNS) {
            var matcher = pattern.matcher(detail);
            if (matcher.find()) {
                var attributeName = toPublicAttributeName(matcher.group(1));
                if (!attributeName.isBlank()) {
                    return Optional.of(attributeName);
                }
            }
        }

        return Optional.empty();
    }

    private static String toPublicAttributeName(String field) {
        var attributeName = field.strip();
        if (attributeName.startsWith("attributes.")) {
            attributeName = attributeName.substring("attributes.".length());
        }

        if (attributeName.startsWith("user.attributes.")) {
            attributeName = attributeName.substring("user.attributes.".length());
        }

        return attributeName;
    }

    private static String normalize(String detail) {
        return detail == null ? "" : detail.toLowerCase(Locale.ROOT);
    }

    public record Result(String detail, String messageKey, Object[] messageArgs) {

        public static Result original(String detail) {
            return new Result(detail, null, new Object[0]);
        }

        public static Result localized(String messageKey, Object... messageArgs) {
            return new Result(Messages.getDefault(messageKey, messageArgs), messageKey, messageArgs == null ? new Object[0] : messageArgs.clone());
        }

        public boolean localized() {
            return this.messageKey != null;
        }
    }
}
