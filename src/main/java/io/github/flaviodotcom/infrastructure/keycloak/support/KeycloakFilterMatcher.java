package io.github.flaviodotcom.infrastructure.keycloak.support;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class KeycloakFilterMatcher {

    private KeycloakFilterMatcher() {
    }

    public static boolean matchesText(String filter, String value, boolean exact) {
        if (filter == null) {
            return true;
        }

        if (value == null) {
            return false;
        }

        var normalizedFilter = filter.toLowerCase(Locale.ROOT);
        var normalizedValue = value.toLowerCase(Locale.ROOT);
        return exact
                ? normalizedValue.equals(normalizedFilter)
                : normalizedValue.contains(normalizedFilter);
    }

    public static boolean matchesAttributes(
            Map<String, String> requestedAttributes,
            Map<String, List<String>> currentAttributes,
            boolean exact
    ) {
        for (var requestedAttribute : requestedAttributes.entrySet()) {
            var values = currentAttributes.get(requestedAttribute.getKey());
            if (values == null || values.isEmpty()) {
                return false;
            }

            var matched = values.stream().anyMatch(value -> matchesText(requestedAttribute.getValue(), value, exact));
            if (!matched) {
                return false;
            }
        }

        return true;
    }
}
