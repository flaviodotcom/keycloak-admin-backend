package io.github.flaviodotcom.resources.query;

import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.github.flaviodotcom.dto.pagination.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriInfo;
import lombok.AllArgsConstructor;

import java.util.Set;

@ApplicationScoped
@AllArgsConstructor
public class RoleQueryCriteriaMapper {

    private static final Set<String> ROLE_SORT_FIELDS = Set.of("id", "name", "description");
    private static final Set<String> ROLE_PARAMS = Set.of("name", "exact", "page", "size", "sort", "sortBy");

    private final QueryParameterReader reader;
    private final PageRequestMapper pageRequestMapper;

    public RoleSearchCriteria toCriteria(UriInfo uriInfo) {
        var queryParams = uriInfo.getQueryParameters();
        this.reader.validateSupportedParams(queryParams, ROLE_PARAMS, false);

        return new RoleSearchCriteria(
                this.reader.readString(queryParams, "name"),
                this.reader.readBoolean(queryParams, "exact")
        );
    }

    public PageRequest toPageRequest(UriInfo uriInfo) {
        return this.pageRequestMapper.toPageRequest(uriInfo.getQueryParameters(), ROLE_SORT_FIELDS, "name");
    }
}
