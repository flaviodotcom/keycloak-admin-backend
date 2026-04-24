package io.github.flaviodotcom.domain.shared;

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
            throw new IllegalArgumentException("Attribute name '%s' is reserved for internal search indexes.".formatted(attributeName));
        }
    }
}
