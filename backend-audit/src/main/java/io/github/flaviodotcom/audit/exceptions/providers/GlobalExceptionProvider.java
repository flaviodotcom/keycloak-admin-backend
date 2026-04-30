package io.github.flaviodotcom.audit.exceptions.providers;

import io.github.flaviodotcom.audit.dto.error.ProblemBuilder;
import io.github.flaviodotcom.audit.exceptions.ExceptionLogger;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionProvider implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        ExceptionLogger.log(exception, 500);
        return ProblemBuilder.build(500, "Internal server error", exception.getMessage());
    }
}
