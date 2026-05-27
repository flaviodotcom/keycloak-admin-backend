package io.github.flaviodotcom.audit.i18n;

import java.text.MessageFormat;
import java.util.Locale;

public final class Messages {

    private Messages() {
    }

    public static String getDefault(String key, Object... args) {
        return get(MessageBundleCatalog.defaultLocale(), key, args);
    }

    public static String get(Locale locale, String key, Object... args) {
        var pattern = findMessage(locale, key);
        return MessageFormat.format(pattern, args);
    }

    private static String findMessage(Locale locale, String key) {
        return MessageBundleCatalog.messages(locale).getString(key);
    }
}
