package io.github.flaviodotcom.audit.resources.query;

import io.github.flaviodotcom.audit.dto.AuditEventFilter;
import io.github.flaviodotcom.audit.dto.pagination.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriInfo;
import lombok.AllArgsConstructor;

import java.util.Set;

@ApplicationScoped
@AllArgsConstructor
public class AuditEventQueryCriteriaMapper {

    static final Set<String> AUDIT_SORT_FIELDS =
            Set.of(
                    "receivedAt",
                    "eventType",
                    "actorId",
                    "subjectId",
                    "topic"
            );

    private static final Set<String> AUDIT_PARAMS =
            Set.of(
                    "eventType",
                    "correlationId",
                    "actorId",
                    "subjectType",
                    "subjectId",
                    "topic",
                    "receivedFrom",
                    "receivedTo",
                    "page",
                    "size",
                    "sort",
                    "sortBy"
            );

    private final QueryParameterReader reader;
    private final PageRequestMapper pageRequestMapper;

    public AuditEventFilter toCriteria(UriInfo uriInfo) {
        var queryParams = uriInfo.getQueryParameters();

        reader.validateSupportedParams(
                queryParams,
                AUDIT_PARAMS,
                false
        );

        return new AuditEventFilter(
                reader.readString(queryParams, "eventType"),
                reader.readString(queryParams, "correlationId"),
                reader.readString(queryParams, "actorId"),
                reader.readString(queryParams, "subjectType"),
                reader.readString(queryParams, "subjectId"),
                reader.readString(queryParams, "topic"),
                reader.readInstant(queryParams, "receivedFrom"),
                reader.readInstant(queryParams, "receivedTo")
        );
    }

    public PageRequest toPageRequest(UriInfo uriInfo) {
        return pageRequestMapper.toPageRequest(
                uriInfo.getQueryParameters(),
                AUDIT_SORT_FIELDS,
                "receivedAt"
        );
    }
}