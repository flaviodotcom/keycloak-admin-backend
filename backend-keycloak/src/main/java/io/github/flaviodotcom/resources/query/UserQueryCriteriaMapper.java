package io.github.flaviodotcom.resources.query;

import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.dto.pagination.PageRequest;
import io.github.flaviodotcom.dto.user.UserResponseOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriInfo;
import lombok.AllArgsConstructor;

import java.util.Set;

@ApplicationScoped
@AllArgsConstructor
public class UserQueryCriteriaMapper {

    public static final Set<String> USER_SORT_FIELDS = Set.of(
            "id",
            "username",
            "email",
            "firstName",
            "lastName",
            "enabled",
            "createdTimestamp"
    );

    private static final Set<String> USER_PARAMS = Set.of(
            "search",
            "username",
            "email",
            "firstName",
            "lastName",
            "enabled",
            "exact",
            "includeGroups",
            "page",
            "size",
            "sort",
            "sortBy"
    );

    private final QueryParameterReader reader;
    private final PageRequestMapper pageRequestMapper;

    public UserSearchCriteria toCriteria(UriInfo uriInfo) {
        var queryParams = uriInfo.getQueryParameters();
        this.reader.validateSupportedParams(queryParams, USER_PARAMS, true);

        return new UserSearchCriteria(
                this.reader.readString(queryParams, "search"),
                this.reader.readString(queryParams, "username"),
                this.reader.readString(queryParams, "email"),
                this.reader.readString(queryParams, "firstName"),
                this.reader.readString(queryParams, "lastName"),
                this.reader.readBoolean(queryParams, "enabled"),
                this.reader.readBoolean(queryParams, "exact"),
                this.reader.readAttributes(queryParams)
        );
    }

    public UserResponseOptions toResponseOptions(UriInfo uriInfo) {
        return new UserResponseOptions(Boolean.TRUE.equals(this.reader.readBoolean(uriInfo.getQueryParameters(), "includeGroups")));
    }

    public PageRequest toPageRequest(UriInfo uriInfo) {
        return this.pageRequestMapper.toPageRequest(uriInfo.getQueryParameters(), USER_SORT_FIELDS, "username");
    }
}
