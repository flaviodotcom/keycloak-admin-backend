package io.github.flaviodotcom.notification.health;

import io.github.flaviodotcom.notification.config.properties.KafkaProperties;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Readiness
@AllArgsConstructor
@ApplicationScoped
public class KafkaReadinessHealthCheck implements HealthCheck {

    private static final long TIMEOUT_SECONDS = 3;

    private final KafkaProperties kafkaProperties;

    @Override
    public HealthCheckResponse call() {
        var config = Map.<String, Object>of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaProperties.bootstrapServers());
        try (var adminClient = AdminClient.create(config)) {
            adminClient.describeCluster().nodes().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kafka readiness check was interrupted.", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new IllegalStateException("Kafka readiness check failed.", exception);
        }
        return HealthCheckResponse.up("backend-notification-kafka");
    }
}
