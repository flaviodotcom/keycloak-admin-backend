package io.github.flaviodotcom.exceptions.providers;

import io.github.flaviodotcom.exceptions.LocalizedMessage;
import io.github.flaviodotcom.exceptions.ProblemBuilder;
import io.github.flaviodotcom.i18n.HttpLocaleResolver;
import io.github.flaviodotcom.i18n.Messages;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionProvider implements ExceptionMapper<Throwable> {

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(Throwable exception) {
        var locale = HttpLocaleResolver.resolve(this.headers);
        var detail = exception instanceof LocalizedMessage localizedMessage && localizedMessage.messageKey() != null
                ? Messages.get(locale, localizedMessage.messageKey(), localizedMessage.messageArgs())
                : exception.getMessage();
        return ProblemBuilder.build(500, Messages.get(locale, "problem.internal.title"), detail);
    }
}
