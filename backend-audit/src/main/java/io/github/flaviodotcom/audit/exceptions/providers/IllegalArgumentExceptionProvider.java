package io.github.flaviodotcom.audit.exceptions.providers;

import io.github.flaviodotcom.audit.dto.error.ProblemBuilder;
import io.github.flaviodotcom.audit.exceptions.ExceptionLogger;
import io.github.flaviodotcom.audit.exceptions.LocalizedMessage;
import io.github.flaviodotcom.audit.i18n.HttpLocaleResolver;
import io.github.flaviodotcom.audit.i18n.Messages;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class IllegalArgumentExceptionProvider implements ExceptionMapper<IllegalArgumentException> {

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(IllegalArgumentException exception) {
        ExceptionLogger.log(exception, 400);
        var locale = HttpLocaleResolver.resolve(this.headers);
        var detail = exception instanceof LocalizedMessage localizedMessage
                ? Messages.get(locale, localizedMessage.messageKey(), localizedMessage.messageArgs())
                : exception.getMessage();
        return ProblemBuilder.build(400, Messages.get(locale, "problem.bad-request.title"), detail);
    }
}
