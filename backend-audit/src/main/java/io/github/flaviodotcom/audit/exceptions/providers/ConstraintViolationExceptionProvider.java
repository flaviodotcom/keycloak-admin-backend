package io.github.flaviodotcom.audit.exceptions.providers;

import io.github.flaviodotcom.audit.dto.error.Problem;
import io.github.flaviodotcom.audit.dto.error.ProblemBuilder;
import io.github.flaviodotcom.audit.exceptions.ExceptionLogger;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ConstraintViolationExceptionProvider implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        ExceptionLogger.log(exception, 400);
        var problem = new Problem(400, "Invalid request data", "One or more fields are invalid.");
        exception.getConstraintViolations().forEach(violation ->
                problem.addMessage(this.lastFieldName(violation.getPropertyPath()), violation.getMessage())
        );
        return ProblemBuilder.build(problem);
    }

    private String lastFieldName(Path path) {
        Path.Node last = null;
        for (Path.Node node : path) {
            last = node;
        }
        return last == null ? null : last.getName();
    }
}
