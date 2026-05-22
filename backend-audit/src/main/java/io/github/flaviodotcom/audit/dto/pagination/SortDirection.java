package io.github.flaviodotcom.audit.dto.pagination;

public enum SortDirection {
    ASC,
    DESC;

    public static SortDirection fromString(String value) {
        return value == null
                ? DESC
                : SortDirection.valueOf(value.trim().toUpperCase());
    }
}
