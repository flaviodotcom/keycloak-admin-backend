package io.github.flaviodotcom.notification.exceptions.providers;

import io.github.flaviodotcom.notification.dto.error.ProblemBuilder;
import io.github.flaviodotcom.notification.exceptions.ExceptionLogger;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class IllegalArgumentExceptionProvider implements ExceptionMapper<IllegalArgumentException> {

    @Override
    public Response toResponse(IllegalArgumentException exception) {
        ExceptionLogger.log(exception, 400);
        return ProblemBuilder.build(400, "Invalid request data", exception.getMessage());
    }
}
