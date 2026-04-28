package io.github.flaviodotcom.dto.pagination;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public PageResponse {
        content = List.copyOf(content);
    }

    public static <T> PageResponse<T> from(List<T> values, PageRequest request, Comparator<T> comparator) {
        var sortedValues = values.stream()
                .sorted(request.sort().apply(comparator))
                .toList();
        var totalElements = sortedValues.size();
        var fromIndex = Math.min((long) request.page() * request.size(), totalElements);
        var toIndex = Math.min(fromIndex + request.size(), totalElements);
        var totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / request.size());

        return new PageResponse<>(
                sortedValues.subList((int) fromIndex, (int) toIndex),
                request.page(),
                request.size(),
                totalElements,
                totalPages,
                request.page() == 0,
                totalPages == 0 || request.page() >= totalPages - 1
        );
    }

    public <R> PageResponse<R> map(Function<T, R> mapper) {
        return new PageResponse<>(
                this.content.stream().map(mapper).toList(),
                this.page,
                this.size,
                this.totalElements,
                this.totalPages,
                this.first,
                this.last
        );
    }
}
