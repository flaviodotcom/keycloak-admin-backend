package io.github.flaviodotcom.exceptions.providers;

import io.github.flaviodotcom.exceptions.ProblemBuilder;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionProvider implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        return ProblemBuilder.build(500, "Internal server error", exception.getMessage());
    }
}
