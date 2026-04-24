package io.github.flaviodotcom.resources.support;

import io.github.flaviodotcom.domain.identity.criteria.GroupSearchCriteria;
import io.github.flaviodotcom.domain.identity.criteria.RoleSearchCriteria;
import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class QueryCriteriaMapper {

    private static final String ATTRIBUTE_PREFIX = "attr.";
    private static final Set<String> USER_PARAMS = Set.of(
            "search",
            "username",
            "email",
            "firstName",
            "lastName",
            "enabled",
            "exact"
    );
    private static final Set<String> GROUP_PARAMS = Set.of("search", "name", "exact");
    private static final Set<String> ROLE_PARAMS = Set.of("name", "exact");

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

    public RoleSearchCriteria toRoleCriteria(UriInfo uriInfo) {
        var queryParams = uriInfo.getQueryParameters();
        this.validateSupportedParams(queryParams, ROLE_PARAMS, false);

        return new RoleSearchCriteria(
                this.readString(queryParams, "name"),
                this.readBoolean(queryParams, "exact")
        );
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
                throw new BadRequestException("Unsupported query param '%s'.".formatted(parameterName));
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
                throw new BadRequestException("Attribute filter name cannot be blank.");
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

        throw new BadRequestException("Query param '%s' must be 'true' or 'false'.".formatted(name));
    }

    private String readString(MultivaluedMap<String, String> queryParams, String name) {
        return this.readOptionalValue(queryParams, name);
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
            throw new BadRequestException("Query param '%s' must be informed only once.".formatted(name));
        }

        var value = values.getFirst();
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Query param '%s' cannot be blank.".formatted(name));
        }

        return value.strip();
    }
}
