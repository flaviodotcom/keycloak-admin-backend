package io.github.flaviodotcom.audit.dto.pagination;

import io.quarkus.hibernate.orm.panache.PanacheQuery;

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

    public static <T> PageResponse<T> from(PanacheQuery<T> query, PageRequest request) {
        var totalElements = query.count();
        query.page(request.page(), request.size());
        var content = query.list();
        var totalPages = totalElements == 0
                ? 0
                : (int) Math.ceil((double) totalElements / request.size());

        return new PageResponse<>(
                content,
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
