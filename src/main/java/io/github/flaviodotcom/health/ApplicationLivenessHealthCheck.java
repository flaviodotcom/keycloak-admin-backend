package io.github.flaviodotcom.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@Liveness
@ApplicationScoped
public class ApplicationLivenessHealthCheck implements HealthCheck {

    private static final String NAME = "application";

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named(NAME).up().build();
    }
}
