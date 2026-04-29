package io.github.flaviodotcom.audit.health;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.sql.DataSource;
import java.sql.SQLException;

@Readiness
@AllArgsConstructor
@ApplicationScoped
public class DatabaseReadinessHealthCheck implements HealthCheck {

    private final DataSource dataSource;

    @Override
    public HealthCheckResponse call() {
        try {
            try (var connection = this.dataSource.getConnection();
                 var statement = connection.prepareStatement("SELECT 1")) {
                statement.execute();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Database readiness check failed.", exception);
        }
        return HealthCheckResponse.up("backend-audit-database");
    }
}
