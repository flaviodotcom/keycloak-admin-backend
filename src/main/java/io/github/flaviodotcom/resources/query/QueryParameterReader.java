package io.github.flaviodotcom.resources.query;

import io.github.flaviodotcom.dto.pagination.SortDirection;
import io.github.flaviodotcom.exceptions.LocalizedWebApplicationException;
import io.github.flaviodotcom.i18n.Messages;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class QueryParameterReader {

    private static final String ATTRIBUTE_PREFIX = "attr.";
    private static final String ASC_SORT = "asc";
    private static final String DESC_SORT = "desc";

    public void validateSupportedParams(
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

    public Map<String, String> readAttributes(MultivaluedMap<String, String> queryParams) {
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

    public Boolean readBoolean(MultivaluedMap<String, String> queryParams, String name) {
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

    public String readString(MultivaluedMap<String, String> queryParams, String name) {
        return this.readOptionalValue(queryParams, name);
    }

    public Integer readInteger(MultivaluedMap<String, String> queryParams, String name, Integer defaultValue) {
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

    public SortDirection readSortDirection(MultivaluedMap<String, String> queryParams) {
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
