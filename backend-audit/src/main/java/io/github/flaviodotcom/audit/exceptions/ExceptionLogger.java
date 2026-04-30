package io.github.flaviodotcom.audit.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ExceptionLogger {

    private ExceptionLogger() {
    }

    public static void log(Throwable exception, int status) {
        if (status >= 500) {
            log.error(exception.getMessage(), exception);
            return;
        }

        log.warn(exception.getMessage(), exception);
    }
}
