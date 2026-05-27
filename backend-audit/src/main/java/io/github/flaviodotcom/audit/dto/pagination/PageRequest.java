package io.github.flaviodotcom.audit.dto.pagination;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

public record PageRequest(
        @QueryParam("page")
        @DefaultValue("0")
        int page,

        @QueryParam("size")
        @DefaultValue("10")
        int size,

        @QueryParam("sortBy")
        @DefaultValue("receivedAt")
        String sortBy,

        @QueryParam("sort")
        @DefaultValue("DESC")
        SortDirection sort
) {
}
