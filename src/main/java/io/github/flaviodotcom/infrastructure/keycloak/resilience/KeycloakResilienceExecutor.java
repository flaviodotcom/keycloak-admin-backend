package io.github.flaviodotcom.infrastructure.keycloak.resilience;

import io.github.flaviodotcom.exceptions.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

@ApplicationScoped
public class KeycloakResilienceExecutor {

    @Retry(maxRetries = 1, delay = 200, delayUnit = ChronoUnit.MILLIS, abortOn = {
            WebApplicationException.class,
            BusinessException.class
    })
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(
            requestVolumeThreshold = 10,
            failureRatio = 0.5,
            delay = 10,
            delayUnit = ChronoUnit.SECONDS,
            skipOn = {
                    WebApplicationException.class,
                    BusinessException.class
            }
    )
    @Bulkhead(20)
    public <T> T executeRead(Supplier<T> operation) {
        return operation.get();
    }

    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(
            requestVolumeThreshold = 10,
            failureRatio = 0.5,
            delay = 10,
            delayUnit = ChronoUnit.SECONDS,
            skipOn = {
                    WebApplicationException.class,
                    BusinessException.class
            }
    )
    @Bulkhead(20)
    public <T> T executeWrite(Supplier<T> operation) {
        return operation.get();
    }

    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(
            requestVolumeThreshold = 10,
            failureRatio = 0.5,
            delay = 10,
            delayUnit = ChronoUnit.SECONDS,
            skipOn = {
                    WebApplicationException.class,
                    BusinessException.class
            }
    )
    @Bulkhead(20)
    public void executeWrite(KeycloakVoidOperation operation) {
        operation.run();
    }

    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @Bulkhead(5)
    public <T> T executeHealthCheck(Supplier<T> operation) {
        return operation.get();
    }

    @FunctionalInterface
    public interface KeycloakVoidOperation {

        void run();
    }
}
