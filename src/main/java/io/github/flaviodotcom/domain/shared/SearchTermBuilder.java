package io.github.flaviodotcom.domain.shared;

import java.util.LinkedHashSet;
import java.util.List;

public final class SearchTermBuilder {

    private SearchTermBuilder() {
    }

    public static List<String> build(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        var terms = new LinkedHashSet<String>();
        addTerm(terms, value);
        for (var token : value.split("\\s+")) {
            addTerm(terms, token);
        }
        return List.copyOf(terms);
    }

    private static void addTerm(LinkedHashSet<String> terms, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        var strippedValue = value.strip();
        terms.add(strippedValue);
        terms.add(TextFilterMatcher.normalize(strippedValue));
    }
}
