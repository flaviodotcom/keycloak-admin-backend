package io.github.flaviodotcom.audit.exceptions.providers;

import io.github.flaviodotcom.audit.dto.error.ProblemBuilder;
import io.github.flaviodotcom.audit.exceptions.ExceptionLogger;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionProvider implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {
        var response = exception.getResponse();
        var status = response == null ? 500 : response.getStatus();
        ExceptionLogger.log(exception, status);
        var title = response == null || response.getStatusInfo() == null
                ? "HTTP request failed"
                : response.getStatusInfo().getReasonPhrase();
        var detail = response != null && response.hasEntity()
                ? response.readEntity(String.class)
                : exception.getMessage();
        return ProblemBuilder.build(status, title, detail);
    }
}
