package io.github.flaviodotcom.domain.shared;

import java.util.List;
import java.util.Map;

public final class AttributeFilterMatcher {

    private AttributeFilterMatcher() {
    }

    public static boolean matches(Map<String, String> requestedAttributes,
                                  Map<String, List<String>> currentAttributes,
                                  boolean exact) {
        for (var requestedAttribute : requestedAttributes.entrySet()) {
            var values = currentAttributes.get(requestedAttribute.getKey());
            if (values == null || values.isEmpty()) {
                return false;
            }

            var matched = values.stream()
                    .anyMatch(value -> TextFilterMatcher.matches(requestedAttribute.getValue(), value, exact));
            if (!matched) {
                return false;
            }
        }

        return true;
    }
}
