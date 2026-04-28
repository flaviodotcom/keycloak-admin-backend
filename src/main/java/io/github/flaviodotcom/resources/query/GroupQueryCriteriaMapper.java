package io.github.flaviodotcom.resources.query;

import io.github.flaviodotcom.domain.identity.criteria.GroupSearchCriteria;
import io.github.flaviodotcom.dto.pagination.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriInfo;
import lombok.AllArgsConstructor;

import java.util.Set;

@ApplicationScoped
@AllArgsConstructor
public class GroupQueryCriteriaMapper {

    private static final Set<String> GROUP_SORT_FIELDS = Set.of("id", "name", "path");
    private static final Set<String> GROUP_PARAMS = Set.of("search", "name", "exact", "page", "size", "sort", "sortBy");

    private final QueryParameterReader reader;
    private final PageRequestMapper pageRequestMapper;

    public GroupSearchCriteria toCriteria(UriInfo uriInfo) {
        var queryParams = uriInfo.getQueryParameters();
        this.reader.validateSupportedParams(queryParams, GROUP_PARAMS, true);

        return new GroupSearchCriteria(
                this.reader.readString(queryParams, "search"),
                this.reader.readString(queryParams, "name"),
                this.reader.readBoolean(queryParams, "exact"),
                this.reader.readAttributes(queryParams)
        );
    }

    public PageRequest toPageRequest(UriInfo uriInfo) {
        return this.pageRequestMapper.toPageRequest(uriInfo.getQueryParameters(), GROUP_SORT_FIELDS, "name");
    }

    public PageRequest toMembersPageRequest(UriInfo uriInfo) {
        var queryParams = uriInfo.getQueryParameters();
        this.reader.validateSupportedParams(queryParams, PageRequestMapper.PAGE_PARAMS, false);
        return this.pageRequestMapper.toPageRequest(queryParams, UserQueryCriteriaMapper.USER_SORT_FIELDS, "username");
    }
}
