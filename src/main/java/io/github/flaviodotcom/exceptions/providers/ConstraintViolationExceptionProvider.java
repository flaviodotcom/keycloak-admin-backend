package io.github.flaviodotcom.exceptions.providers;

import io.github.flaviodotcom.exceptions.Problem;
import io.github.flaviodotcom.exceptions.ProblemBuilder;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ConstraintViolationExceptionProvider implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
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
