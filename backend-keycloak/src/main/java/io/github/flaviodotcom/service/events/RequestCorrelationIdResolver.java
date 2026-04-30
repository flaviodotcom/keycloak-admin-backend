package io.github.flaviodotcom.service.events;

import io.github.flaviodotcom.config.interceptors.LoggingFilter;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import lombok.AllArgsConstructor;

@RequestScoped
@AllArgsConstructor
public class RequestCorrelationIdResolver {

    private final ContainerRequestContext requestContext;

    public String resolve() {
        var correlationId = this.requestContext.getProperty(LoggingFilter.CORRELATION_ID_PROPERTY);
        if (correlationId == null || correlationId.toString().isBlank()) {
            throw new IllegalStateException("Request correlation id was not initialized.");
        }
        return correlationId.toString();
    }
}
