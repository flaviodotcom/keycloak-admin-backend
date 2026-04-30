package io.github.flaviodotcom.config.interceptors;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Provider
@PreMatching
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_PROPERTY = LoggingFilter.class.getName() + ".correlationId";

    private static final String START_NANOS_PROPERTY = LoggingFilter.class.getName() + ".startNanos";

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        var correlationId = this.resolveCorrelationId(containerRequestContext.getHeaderString(CORRELATION_ID_HEADER));
        containerRequestContext.setProperty(CORRELATION_ID_PROPERTY, correlationId);
        containerRequestContext.setProperty(START_NANOS_PROPERTY, System.nanoTime());

        log.info("[Start Request - Timestamp: {} - CorrelationId: {} - Method: {} - Path: {}]",
                getNow(),
                correlationId,
                containerRequestContext.getMethod(),
                containerRequestContext.getUriInfo().getPath()
        );
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) {
        var correlationId = Objects.requireNonNull(containerRequestContext.getProperty(CORRELATION_ID_PROPERTY)).toString();
        var startNanos = (Long) Objects.requireNonNull(containerRequestContext.getProperty(START_NANOS_PROPERTY));
        var durationMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        containerResponseContext.getHeaders().putSingle(CORRELATION_ID_HEADER, correlationId);

        log.info("[End Request - Timestamp: {} - CorrelationId: {} - Status: {} - DurationMs: {}]",
                getNow(),
                correlationId,
                containerResponseContext.getStatus(),
                durationMillis
        );
    }

    private LocalDateTime getNow() {
        return LocalDateTime.now().withNano(0);
    }

    private String resolveCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return correlationId.strip();
    }
}
