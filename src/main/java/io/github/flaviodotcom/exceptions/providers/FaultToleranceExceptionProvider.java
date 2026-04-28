package io.github.flaviodotcom.exceptions.providers;

import io.github.flaviodotcom.dto.error.ProblemBuilder;
import io.github.flaviodotcom.exceptions.ExceptionLogger;
import io.github.flaviodotcom.i18n.HttpLocaleResolver;
import io.github.flaviodotcom.i18n.Messages;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException;
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceException;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;

@Provider
public class FaultToleranceExceptionProvider implements ExceptionMapper<FaultToleranceException> {

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(FaultToleranceException exception) {
        var locale = HttpLocaleResolver.resolve(this.headers);
        var status = this.status(exception);
        ExceptionLogger.log(exception, status);
        return ProblemBuilder.build(
                status,
                Messages.get(locale, "http.status.%s.title".formatted(status)),
                Messages.get(locale, this.messageKey(exception))
        );
    }

    private int status(FaultToleranceException exception) {
        if (exception instanceof TimeoutException) {
            return 504;
        }

        return 503;
    }

    private String messageKey(FaultToleranceException exception) {
        if (exception instanceof TimeoutException) {
            return "error.keycloak.timeout";
        }
        if (exception instanceof CircuitBreakerOpenException) {
            return "error.keycloak.circuit-open";
        }
        if (exception instanceof BulkheadException) {
            return "error.keycloak.bulkhead-rejected";
        }

        return "error.keycloak.unavailable";
    }
}
