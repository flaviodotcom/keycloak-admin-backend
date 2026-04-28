package io.github.flaviodotcom.exceptions.providers;

import io.github.flaviodotcom.exceptions.BusinessException;
import io.github.flaviodotcom.exceptions.ExceptionLogger;
import io.github.flaviodotcom.exceptions.LocalizedMessage;
import io.github.flaviodotcom.dto.error.ProblemBuilder;
import io.github.flaviodotcom.i18n.HttpLocaleResolver;
import io.github.flaviodotcom.i18n.Messages;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BusinessExceptionProvider implements ExceptionMapper<BusinessException> {

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(BusinessException exception) {
        ExceptionLogger.log(exception, 422);
        var locale = HttpLocaleResolver.resolve(this.headers);
        var detail = this.localizedMessage(exception, locale);
        return ProblemBuilder.build(422, Messages.get(locale, "problem.business.title"), detail);
    }

    private String localizedMessage(LocalizedMessage message, java.util.Locale locale) {
        if (message.messageKey() == null) {
            return message.fallbackMessage();
        }

        return Messages.get(locale, message.messageKey(), message.messageArgs());
    }
}
