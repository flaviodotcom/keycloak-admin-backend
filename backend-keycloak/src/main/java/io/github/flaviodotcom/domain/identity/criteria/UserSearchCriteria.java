package io.github.flaviodotcom.domain.identity.criteria;

import java.util.Map;

public record UserSearchCriteria(
        String search,
        String username,
        String email,
        String firstName,
        String lastName,
        Boolean enabled,
        Boolean exact,
        Map<String, String> attributes
) {

    public UserSearchCriteria {
        attributes = attributes == null ? Map.of() : attributes;
        exact = exact == null ? Boolean.FALSE : exact;
    }

    public boolean hasAttributeFilters() {
        return !this.attributes.isEmpty();
    }

    public boolean hasSearchTerm() {
        return this.search != null && !this.search.isBlank();
    }
}
