package io.github.flaviodotcom.domain.identity.criteria;

public record RoleSearchCriteria(
        String name,
        Boolean exact
) {

    public RoleSearchCriteria {
        exact = exact == null ? Boolean.FALSE : exact;
    }
}
