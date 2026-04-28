package io.github.flaviodotcom.health;

import io.github.flaviodotcom.infrastructure.keycloak.resilience.KeycloakResilienceExecutor;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
@AllArgsConstructor
public class KeycloakReadinessHealthCheck implements HealthCheck {

    private static final String NAME = "keycloak";

    private final KeycloakAdminSupport keycloak;
    private final KeycloakResilienceExecutor resilience;

    @Override
    public HealthCheckResponse call() {
        this.resilience.executeHealthCheck(() -> this.keycloak.realm().toRepresentation());
        return HealthCheckResponse.named(NAME).up().build();
    }
}
