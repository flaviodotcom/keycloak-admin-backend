package io.github.flaviodotcom.audit.exceptions.providers;

import io.github.flaviodotcom.audit.dto.error.ProblemBuilder;
import io.github.flaviodotcom.audit.exceptions.ExceptionLogger;
import io.github.flaviodotcom.audit.exceptions.LocalizedMessage;
import io.github.flaviodotcom.audit.i18n.HttpLocaleResolver;
import io.github.flaviodotcom.audit.i18n.Messages;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionProvider implements ExceptionMapper<WebApplicationException> {

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(WebApplicationException exception) {
        var locale = HttpLocaleResolver.resolve(this.headers);
        var response = exception.getResponse();
        var status = response == null ? 500 : response.getStatus();
        ExceptionLogger.log(exception, status);
        var title = response == null || response.getStatusInfo() == null
                ? Messages.get(locale, "problem.http.title")
                : Messages.get(locale, "http.status.%s.title".formatted(status));
        var detail = response != null && response.hasEntity()
                ? response.readEntity(String.class)
                : exception.getMessage();
        if (exception instanceof LocalizedMessage localizedMessage && localizedMessage.messageKey() != null) {
            detail = Messages.get(locale, localizedMessage.messageKey(), localizedMessage.messageArgs());
        }
        return ProblemBuilder.build(status, title, detail);
    }
}
