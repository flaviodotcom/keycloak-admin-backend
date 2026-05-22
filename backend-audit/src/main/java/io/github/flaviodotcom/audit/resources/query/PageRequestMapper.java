package io.github.flaviodotcom.audit.resources.query;

import io.github.flaviodotcom.audit.dto.pagination.PageRequest;
import io.github.flaviodotcom.audit.exceptions.LocalizedWebApplicationException;
import io.github.flaviodotcom.audit.i18n.Messages;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.AllArgsConstructor;

import java.util.Set;

@ApplicationScoped
@AllArgsConstructor
public class PageRequestMapper {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private final QueryParameterReader reader;

    public PageRequest toPageRequest(MultivaluedMap<String, String> queryParams,
                                     Set<String> supportedSortFields,
                                     String defaultSortBy) {
        var page = this.reader.readInteger(queryParams, "page", DEFAULT_PAGE);
        var size = this.reader.readInteger(queryParams, "size", DEFAULT_SIZE);
        if (page < 0) {
            throw badRequest("error.query-param.min", "page", 0);
        }
        if (size < 1) {
            throw badRequest("error.query-param.min", "size", 1);
        }
        if (size > MAX_SIZE) {
            throw badRequest("error.query-param.max", "size", MAX_SIZE);
        }

        var sortBy = this.reader.readString(queryParams, "sortBy");
        sortBy = sortBy == null ? defaultSortBy : sortBy;
        if (!supportedSortFields.contains(sortBy)) {
            throw badRequest("error.query-param.unsupported-sort-by", sortBy);
        }

        return new PageRequest(page, size, sortBy, this.reader.readSortDirection(queryParams));
    }

    private LocalizedWebApplicationException badRequest(String messageKey, Object... messageArgs) {
        return new LocalizedWebApplicationException(400, messageKey, Messages.getDefault(messageKey, messageArgs), messageArgs);
    }
}
