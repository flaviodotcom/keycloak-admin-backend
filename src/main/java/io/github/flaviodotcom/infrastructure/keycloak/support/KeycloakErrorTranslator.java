package io.github.flaviodotcom.infrastructure.keycloak.support;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public final class KeycloakErrorTranslator {

    private static final String USERNAME_CONFLICT_MESSAGE = "A user already exists with the provided username.";
    private static final String EMAIL_CONFLICT_MESSAGE = "A user already exists with the provided email.";
    private static final String USERNAME_OR_EMAIL_CONFLICT_MESSAGE = "A user already exists with the provided username or email.";
    private static final String REQUIRED_ATTRIBUTE_MESSAGE = "A required user attribute was not provided.";
    private static final String REQUIRED_ATTRIBUTE_MESSAGE_PATTERN = "Required user attribute '%s' was not provided.";

    private static final List<Pattern> ATTRIBUTE_NAME_PATTERNS = List.of(
            Pattern.compile("\"field\"\\s*:\\s*\"([^\"]+)\""),
            Pattern.compile("\"attribute\"\\s*:\\s*\"([^\"]+)\"")
    );

    private KeycloakErrorTranslator() {
    }

    public static String translate(int status, String detail) {
        var normalizedDetail = normalize(detail);
        if (isUserConflict(status, normalizedDetail)) {
            return translateUserConflict(normalizedDetail);
        }

        if (isRequiredUserAttribute(status, normalizedDetail)) {
            return translateRequiredUserAttribute(detail);
        }

        return detail;
    }

    private static boolean isUserConflict(int status, String normalizedDetail) {
        return status == 409
                && normalizedDetail.contains("user exists")
                && (normalizedDetail.contains("same username")
                || normalizedDetail.contains("same email")
                || normalizedDetail.contains("same username or email"));
    }

    private static String translateUserConflict(String normalizedDetail) {
        if (normalizedDetail.contains("same username or email")) {
            return USERNAME_OR_EMAIL_CONFLICT_MESSAGE;
        }

        if (normalizedDetail.contains("same username")) {
            return USERNAME_CONFLICT_MESSAGE;
        }

        if (normalizedDetail.contains("same email")) {
            return EMAIL_CONFLICT_MESSAGE;
        }

        return USERNAME_OR_EMAIL_CONFLICT_MESSAGE;
    }

    private static boolean isRequiredUserAttribute(int status, String normalizedDetail) {
        return status == 400
                && (normalizedDetail.contains("error-user-attribute-required")
                || normalizedDetail.contains("up-attribute-required")
                || normalizedDetail.contains("user attribute") && normalizedDetail.contains("required"));
    }

    private static String translateRequiredUserAttribute(String detail) {
        return extractAttributeName(detail)
                .map(REQUIRED_ATTRIBUTE_MESSAGE_PATTERN::formatted)
                .orElse(REQUIRED_ATTRIBUTE_MESSAGE);
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
}
