package io.github.flaviodotcom.audit.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class MessageBundleCatalog {

    static final String MESSAGES_BUNDLE_NAME = "messages";

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static final ResourceBundle.Control NO_JVM_LOCALE_FALLBACK = new ResourceBundle.Control() {
        @Override
        public Locale getFallbackLocale(String baseName, Locale locale) {
            return null;
        }
    };

    private MessageBundleCatalog() {
    }

    public static Locale defaultLocale() {
        return DEFAULT_LOCALE;
    }

    public static Locale supportedOrDefault(Locale locale) {
        if (locale == null || locale.getLanguage().isBlank() || DEFAULT_LOCALE.getLanguage().equals(locale.getLanguage())) {
            return DEFAULT_LOCALE;
        }

        if (isSupported(locale)) {
            return locale;
        }

        return DEFAULT_LOCALE;
    }

    public static boolean isSupported(Locale locale) {
        if (locale == null || locale.getLanguage().isBlank()) {
            return false;
        }

        if (DEFAULT_LOCALE.getLanguage().equals(locale.getLanguage())) {
            return true;
        }

        return hasLocalizedBundle(locale);
    }

    public static ResourceBundle messages(Locale locale) {
        return ResourceBundle.getBundle(MESSAGES_BUNDLE_NAME, supportedOrDefault(locale), NO_JVM_LOCALE_FALLBACK);
    }

    private static boolean hasLocalizedBundle(Locale locale) {
        try {
            var bundle = ResourceBundle.getBundle(MESSAGES_BUNDLE_NAME, locale, NO_JVM_LOCALE_FALLBACK);
            return !bundle.getLocale().equals(Locale.ROOT);
        } catch (MissingResourceException exception) {
            return false;
        }
    }
}
