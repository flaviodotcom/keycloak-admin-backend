package io.github.flaviodotcom.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class MessageBundleCatalog {

    static final String MESSAGES_BUNDLE_NAME = "messages";
    static final String VALIDATION_MESSAGES_BUNDLE_NAME = "ValidationMessages";

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

        return hasLocalizedBundle(MESSAGES_BUNDLE_NAME, locale)
                && hasLocalizedBundle(VALIDATION_MESSAGES_BUNDLE_NAME, locale);
    }

    public static ResourceBundle messages(Locale locale) {
        return ResourceBundle.getBundle(MESSAGES_BUNDLE_NAME, supportedOrDefault(locale), NO_JVM_LOCALE_FALLBACK);
    }

    public static ResourceBundle validationMessages(Locale locale) {
        return ResourceBundle.getBundle(VALIDATION_MESSAGES_BUNDLE_NAME, supportedOrDefault(locale), NO_JVM_LOCALE_FALLBACK);
    }

    private static boolean hasLocalizedBundle(String bundleName, Locale locale) {
        try {
            var bundle = ResourceBundle.getBundle(bundleName, locale, NO_JVM_LOCALE_FALLBACK);
            return !bundle.getLocale().equals(Locale.ROOT);
        } catch (MissingResourceException exception) {
            return false;
        }
    }
}
