package io.github.flaviodotcom.i18n;

import jakarta.ws.rs.core.HttpHeaders;

import java.util.Locale;

public final class HttpLocaleResolver {

    private HttpLocaleResolver() {
    }

    public static Locale resolve(HttpHeaders headers) {
        if (headers == null) {
            return MessageBundleCatalog.defaultLocale();
        }

        return resolve(headers.getHeaderString(HttpHeaders.ACCEPT_LANGUAGE));
    }

    public static Locale resolve(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return MessageBundleCatalog.defaultLocale();
        }

        var languageRanges = Locale.LanguageRange.parse(acceptLanguage).stream()
                .filter(range -> !"*".equals(range.getRange()))
                .toList();
        if (languageRanges.isEmpty()) {
            return MessageBundleCatalog.defaultLocale();
        }

        return languageRanges.stream()
                .map(range -> Locale.forLanguageTag(range.getRange()))
                .filter(MessageBundleCatalog::isSupported)
                .findFirst()
                .map(MessageBundleCatalog::supportedOrDefault)
                .orElse(MessageBundleCatalog.defaultLocale());
    }
}
