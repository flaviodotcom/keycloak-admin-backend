package io.github.flaviodotcom.domain.shared;

import io.github.flaviodotcom.exceptions.LocalizedIllegalArgumentException;
import io.github.flaviodotcom.i18n.Messages;

public final class SearchableAttributeName {

    private static final String INTERNAL_PREFIX = "__search_";

    private SearchableAttributeName() {
    }

    public static String toInternalName(String attributeName) {
        return "%s%s".formatted(INTERNAL_PREFIX, attributeName);
    }

    public static boolean isInternalName(String attributeName) {
        return attributeName != null && attributeName.startsWith(INTERNAL_PREFIX);
    }

    public static void requirePublicName(String attributeName) {
        if (isInternalName(attributeName)) {
            var messageKey = "error.attribute-name.internal-reserved";
            throw new LocalizedIllegalArgumentException(messageKey, Messages.getDefault(messageKey, attributeName), attributeName);
        }
    }
}
