package io.github.flaviodotcom.service.pagination;

import io.github.flaviodotcom.domain.identity.model.IdentityGroup;
import io.github.flaviodotcom.domain.identity.model.IdentityRole;
import io.github.flaviodotcom.domain.identity.model.IdentityUser;
import io.github.flaviodotcom.dto.pagination.PageRequest;
import io.github.flaviodotcom.exceptions.LocalizedIllegalArgumentException;
import io.github.flaviodotcom.i18n.Messages;

import java.util.Comparator;

public final class IdentitySortComparators {

    private static final String UNSUPPORTED_SORT_FIELD = "error.query-param.unsupported-sort-by";

    private IdentitySortComparators() {
    }

    public static Comparator<IdentityUser> userComparator(PageRequest pageRequest) {
        Comparator<IdentityUser> comparator = switch (pageRequest.sortBy()) {
            case "id" -> Comparator.comparing(IdentityUser::id, Comparator.nullsLast(String::compareToIgnoreCase));
            case "username" -> Comparator.comparing(IdentityUser::username, Comparator.nullsLast(String::compareToIgnoreCase));
            case "email" -> Comparator.comparing(IdentityUser::email, Comparator.nullsLast(String::compareToIgnoreCase));
            case "firstName" -> Comparator.comparing(IdentityUser::firstName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "lastName" -> Comparator.comparing(IdentityUser::lastName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "enabled" -> Comparator.comparing(IdentityUser::enabled, Comparator.nullsLast(Boolean::compareTo));
            case "createdTimestamp" -> Comparator.comparing(IdentityUser::createdTimestamp, Comparator.nullsLast(Long::compareTo));
            default -> throw unsupportedSortField(pageRequest.sortBy());
        };
        return comparator.thenComparing(IdentityUser::id, Comparator.nullsLast(String::compareToIgnoreCase));
    }

    public static Comparator<IdentityGroup> groupComparator(PageRequest pageRequest) {
        Comparator<IdentityGroup> comparator = switch (pageRequest.sortBy()) {
            case "id" -> Comparator.comparing(IdentityGroup::id, Comparator.nullsLast(String::compareToIgnoreCase));
            case "name" -> Comparator.comparing(IdentityGroup::name, Comparator.nullsLast(String::compareToIgnoreCase));
            case "path" -> Comparator.comparing(IdentityGroup::path, Comparator.nullsLast(String::compareToIgnoreCase));
            default -> throw unsupportedSortField(pageRequest.sortBy());
        };
        return comparator.thenComparing(IdentityGroup::id, Comparator.nullsLast(String::compareToIgnoreCase));
    }

    public static Comparator<IdentityRole> roleComparator(PageRequest pageRequest) {
        Comparator<IdentityRole> comparator = switch (pageRequest.sortBy()) {
            case "id" -> Comparator.comparing(IdentityRole::id, Comparator.nullsLast(String::compareToIgnoreCase));
            case "name" -> Comparator.comparing(IdentityRole::name, Comparator.nullsLast(String::compareToIgnoreCase));
            case "description" -> Comparator.comparing(IdentityRole::description, Comparator.nullsLast(String::compareToIgnoreCase));
            default -> throw unsupportedSortField(pageRequest.sortBy());
        };
        return comparator.thenComparing(IdentityRole::id, Comparator.nullsLast(String::compareToIgnoreCase));
    }

    private static LocalizedIllegalArgumentException unsupportedSortField(String sortBy) {
        return new LocalizedIllegalArgumentException(
                UNSUPPORTED_SORT_FIELD,
                Messages.getDefault(UNSUPPORTED_SORT_FIELD, sortBy),
                sortBy
        );
    }
}
