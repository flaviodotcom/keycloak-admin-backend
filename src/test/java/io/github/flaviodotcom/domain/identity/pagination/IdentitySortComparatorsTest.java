package io.github.flaviodotcom.domain.identity.pagination;

import io.github.flaviodotcom.dto.pagination.PageRequest;
import io.github.flaviodotcom.dto.pagination.SortDirection;
import io.github.flaviodotcom.exceptions.LocalizedIllegalArgumentException;
import io.github.flaviodotcom.i18n.Messages;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdentitySortComparatorsTest {

    @Test
    void givenUnsupportedUserSortField_WhenBuildComparator_ThenThrowLocalizedException() {
        var pageRequest = new PageRequest(0, 10, "unsupported", SortDirection.ASC);

        var exception = assertThrows(
                LocalizedIllegalArgumentException.class,
                () -> IdentitySortComparators.userComparator(pageRequest)
        );

        assertUnsupportedSortField(exception);
    }

    @Test
    void givenUnsupportedGroupSortField_WhenBuildComparator_ThenThrowLocalizedException() {
        var pageRequest = new PageRequest(0, 10, "unsupported", SortDirection.ASC);

        var exception = assertThrows(
                LocalizedIllegalArgumentException.class,
                () -> IdentitySortComparators.groupComparator(pageRequest)
        );

        assertUnsupportedSortField(exception);
    }

    @Test
    void givenUnsupportedRoleSortField_WhenBuildComparator_ThenThrowLocalizedException() {
        var pageRequest = new PageRequest(0, 10, "unsupported", SortDirection.ASC);

        var exception = assertThrows(
                LocalizedIllegalArgumentException.class,
                () -> IdentitySortComparators.roleComparator(pageRequest)
        );

        assertUnsupportedSortField(exception);
    }

    private static void assertUnsupportedSortField(LocalizedIllegalArgumentException exception) {
        assertEquals("error.query-param.unsupported-sort-by", exception.messageKey());
        assertArrayEquals(new Object[]{"unsupported"}, exception.messageArgs());
        assertEquals(
                "Campo de ordenação 'unsupported' não suportado.",
                Messages.get(Locale.forLanguageTag("pt-BR"), exception.messageKey(), exception.messageArgs())
        );
    }
}
