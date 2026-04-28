package io.github.flaviodotcom.resources.support;

import io.github.flaviodotcom.exceptions.LocalizedWebApplicationException;
import io.github.flaviodotcom.i18n.Messages;
import io.github.flaviodotcom.domain.identity.criteria.GroupSearchCriteria;
import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.dto.pagination.PageRequest;
import io.github.flaviodotcom.dto.pagination.SortDirection;
import io.github.flaviodotcom.dto.UserResponseOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class QueryCriteriaMapper {

    private static final String ATTRIBUTE_PREFIX = "attr.";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;
    private static final String ASC_SORT = "asc";
    private static final String DESC_SORT = "desc";
    private static final Set<String> PAGE_PARAMS = Set.of("page", "size", "sort", "sortBy");
    private static final Set<String> USER_SORT_FIELDS = Set.of(
            "id",
            "username",
            "email",
            "firstName",
            "lastName",
            "enabled",
            "createdTimestamp"
    );
    private static final Set<String> GROUP_SORT_FIELDS = Set.of("id", "name", "path");
    private static final Set<String> ROLE_SORT_FIELDS = Set.of("id", "name", "description");
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
    private static final Set<String> GROUP_PARAMS = Set.of("search", "name", "exact", "page", "size", "sort", "sortBy");
    private static final Set<String> ROLE_PARAMS = Set.of("name", "exact", "page", "size", "sort", "sortBy");

    public UserSearchCriteria toUserCriteria(UriInfo uriInfo) {
        var queryParams = uriInfo.getQueryParameters();
        this.validateSupportedParams(queryParams, USER_PARAMS, true);

        return new UserSearchCriteria(
                this.readString(queryParams, "search"),
                this.readString(queryParams, "username"),
                this.readString(queryParams, "email"),
                this.readString(queryParams, "firstName"),
                this.readString(queryParams, "lastName"),
                this.readBoolean(queryParams, "enabled"),
                this.readBoolean(queryParams, "exact"),
                this.readAttributes(queryParams)
        );
    }

    public UserResponseOptions toUserResponseOptions(UriInfo uriInfo) {
        return new UserResponseOptions(Boolean.TRUE.equals(this.readBoolean(uriInfo.getQueryParameters(), "includeGroups")));
    }

    public PageRequest toUserPageRequest(UriInfo uriInfo) {
        return this.toPageRequest(uriInfo.getQueryParameters(), USER_SORT_FIELDS, "username");
    }

    public GroupSearchCriteria toGroupCriteria(UriInfo uriInfo) {
        var queryParams = uriInfo.getQueryParameters();
        this.validateSupportedParams(queryParams, GROUP_PARAMS, true);

        return new GroupSearchCriteria(
                this.readString(queryParams, "search"),
                this.readString(queryParams, "name"),
                this.readBoolean(queryParams, "exact"),
                this.readAttributes(queryParams)
        );
    }

    public PageRequest toGroupPageRequest(UriInfo uriInfo) {
        return this.toPageRequest(uriInfo.getQueryParameters(), GROUP_SORT_FIELDS, "name");
    }

    public PageRequest toGroupMembersPageRequest(UriInfo uriInfo) {
        var queryParams = uriInfo.getQueryParameters();
        this.validateSupportedParams(queryParams, PAGE_PARAMS, false);
        return this.toPageRequest(queryParams, USER_SORT_FIELDS, "username");
    }

    public RoleSearchCriteria toRoleCriteria(UriInfo uriInfo) {
        var queryParams = uriInfo.getQueryParameters();
        this.validateSupportedParams(queryParams, ROLE_PARAMS, false);

        return new RoleSearchCriteria(
                this.readString(queryParams, "name"),
                this.readBoolean(queryParams, "exact")
        );
    }

    public PageRequest toRolePageRequest(UriInfo uriInfo) {
        return this.toPageRequest(uriInfo.getQueryParameters(), ROLE_SORT_FIELDS, "name");
    }

    private PageRequest toPageRequest(MultivaluedMap<String, String> queryParams,
                                      Set<String> supportedSortFields,
                                      String defaultSortBy) {
        var page = this.readInteger(queryParams, "page", DEFAULT_PAGE);
        var size = this.readInteger(queryParams, "size", DEFAULT_SIZE);
        if (page < 0) {
            throw badRequest("error.query-param.min", "page", 0);
        }
        if (size < 1) {
            throw badRequest("error.query-param.min", "size", 1);
        }
        if (size > MAX_SIZE) {
            throw badRequest("error.query-param.max", "size", MAX_SIZE);
        }

        var sortBy = this.readString(queryParams, "sortBy");
        sortBy = sortBy == null ? defaultSortBy : sortBy;
        if (!supportedSortFields.contains(sortBy)) {
            throw badRequest("error.query-param.unsupported-sort-by", sortBy);
        }

        return new PageRequest(page, size, sortBy, this.readSortDirection(queryParams));
    }

    private void validateSupportedParams(
            MultivaluedMap<String, String> queryParams,
            Set<String> allowedParams,
            boolean allowAttributes
    ) {
        for (var parameterName : queryParams.keySet()) {
            var allowed = allowedParams.contains(parameterName)
                    || allowAttributes && parameterName.startsWith(ATTRIBUTE_PREFIX);
            if (!allowed) {
                throw badRequest("error.query-param.unsupported", parameterName);
            }
        }
    }

    private Map<String, String> readAttributes(MultivaluedMap<String, String> queryParams) {
        var attributes = new LinkedHashMap<String, String>();
        for (var parameter : queryParams.entrySet()) {
            if (!parameter.getKey().startsWith(ATTRIBUTE_PREFIX)) {
                continue;
            }

            var attributeName = parameter.getKey().substring(ATTRIBUTE_PREFIX.length()).strip();
            if (attributeName.isBlank()) {
                throw badRequest("error.attribute-filter.blank-name");
            }

            attributes.put(attributeName, this.readSingleValue(parameter.getKey(), parameter.getValue()));
        }
        return Map.copyOf(attributes);
    }

    private Boolean readBoolean(MultivaluedMap<String, String> queryParams, String name) {
        var value = this.readOptionalValue(queryParams, name);
        if (value == null) {
            return null;
        }

        if ("true".equalsIgnoreCase(value)) {
            return true;
        }

        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        throw badRequest("error.query-param.invalid-boolean", name);
    }

    private String readString(MultivaluedMap<String, String> queryParams, String name) {
        return this.readOptionalValue(queryParams, name);
    }

    private Integer readInteger(MultivaluedMap<String, String> queryParams, String name, Integer defaultValue) {
        var value = this.readOptionalValue(queryParams, name);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            throw badRequest("error.query-param.invalid-integer", name);
        }
    }

    private SortDirection readSortDirection(MultivaluedMap<String, String> queryParams) {
        var value = this.readOptionalValue(queryParams, "sort");
        if (value == null || ASC_SORT.equalsIgnoreCase(value)) {
            return SortDirection.ASC;
        }

        if (DESC_SORT.equalsIgnoreCase(value)) {
            return SortDirection.DESC;
        }

        throw badRequest("error.query-param.invalid-sort", "sort");
    }

    private String readOptionalValue(MultivaluedMap<String, String> queryParams, String name) {
        var values = queryParams.get(name);
        if (values == null) {
            return null;
        }
        return this.readSingleValue(name, values);
    }

    private String readSingleValue(String name, List<String> values) {
        if (values.size() != 1) {
            throw badRequest("error.query-param.multiple-values", name);
        }

        var value = values.getFirst();
        if (value == null || value.isBlank()) {
            throw badRequest("error.query-param.blank", name);
        }

        return value.strip();
    }

    private LocalizedWebApplicationException badRequest(String messageKey, Object... messageArgs) {
        return new LocalizedWebApplicationException(400, messageKey, Messages.getDefault(messageKey, messageArgs), messageArgs);
    }
}
