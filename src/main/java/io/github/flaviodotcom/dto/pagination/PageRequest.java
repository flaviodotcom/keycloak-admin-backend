package io.github.flaviodotcom.dto.pagination;

public record PageRequest(
        int page,
        int size,
        String sortBy,
        SortDirection sort
) {
}
