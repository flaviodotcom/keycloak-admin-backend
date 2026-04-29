package io.github.flaviodotcom.exceptions;

import io.quarkus.logging.Log;

public final class ExceptionLogger {

    private ExceptionLogger() {
    }

    public static void log(Throwable exception, int status) {
        if (status >= 500) {
            Log.error(exception.getMessage(), exception);
            return;
        }

        Log.warn(exception.getMessage(), exception);
    }
}
