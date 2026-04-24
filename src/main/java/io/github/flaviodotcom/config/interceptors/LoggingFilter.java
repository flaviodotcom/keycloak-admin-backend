package io.github.flaviodotcom.config.interceptors;

import io.quarkus.logging.Log;
import io.vertx.core.http.HttpServerRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;

@Provider
class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    @Context
    UriInfo info;

    @Context
    HttpServerRequest request;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        Log.info("[Start Request - " +
                "Timestamp: " + getNow() + " - " +
                "Method: " + containerRequestContext.getMethod() + " - " +
                "Path: " + containerRequestContext.getUriInfo().getPath() +
                "]");
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) {
        Log.info("[End Request - Timestamp: " + getNow() + "]");
    }

    private LocalDateTime getNow() {
        return LocalDateTime.now().withNano(0);
    }
}
