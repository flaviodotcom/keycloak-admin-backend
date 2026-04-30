package io.github.flaviodotcom.exceptions.providers;

import io.github.flaviodotcom.exceptions.ExceptionLogger;
import io.github.flaviodotcom.dto.error.Problem;
import io.github.flaviodotcom.dto.error.ProblemBuilder;
import io.github.flaviodotcom.i18n.HttpLocaleResolver;
import io.github.flaviodotcom.i18n.Messages;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ConstraintViolationExceptionProvider implements ExceptionMapper<ConstraintViolationException> {

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        ExceptionLogger.log(exception, 400);
        var locale = HttpLocaleResolver.resolve(this.headers);
        var problem = new Problem(
                400,
                Messages.get(locale, "problem.validation.title"),
                Messages.get(locale, "problem.validation.detail")
        );
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
