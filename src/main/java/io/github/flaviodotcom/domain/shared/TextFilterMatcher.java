package io.github.flaviodotcom.domain.shared;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class TextFilterMatcher {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{M}+");

    private TextFilterMatcher() {
    }

    public static boolean matches(String filter, String value, boolean exact) {
        if (filter == null) {
            return true;
        }

        if (value == null) {
            return false;
        }

        var normalizedFilter = normalize(filter);
        var normalizedValue = normalize(value);
        return exact
                ? normalizedValue.equals(normalizedFilter)
                : normalizedValue.contains(normalizedFilter);
    }

    public static String normalize(String value) {
        var decomposedValue = Normalizer.normalize(value, Normalizer.Form.NFD);
        return DIACRITICS_PATTERN.matcher(decomposedValue)
                .replaceAll("")
                .toLowerCase(Locale.ROOT);
    }
}
