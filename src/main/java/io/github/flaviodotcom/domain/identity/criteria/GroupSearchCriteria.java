package io.github.flaviodotcom.domain.identity.criteria;

import java.util.Map;

public record GroupSearchCriteria(
        String search,
        String name,
        Boolean exact,
        Map<String, String> attributes
) {

    public GroupSearchCriteria {
        attributes = attributes == null ? Map.of() : attributes;
        exact = exact == null ? Boolean.FALSE : exact;
    }
}
