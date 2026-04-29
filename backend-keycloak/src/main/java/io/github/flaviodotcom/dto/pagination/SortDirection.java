package io.github.flaviodotcom.dto.pagination;

import java.util.Comparator;

public enum SortDirection {
    ASC,
    DESC;

    public <T> Comparator<T> apply(Comparator<T> comparator) {
        return this == DESC ? comparator.reversed() : comparator;
    }
}
