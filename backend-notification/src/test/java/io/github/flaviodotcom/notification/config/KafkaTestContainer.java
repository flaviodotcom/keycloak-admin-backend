package io.github.flaviodotcom.notification.config;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class KafkaTestContainer implements QuarkusTestResourceLifecycleManager {

    private KafkaContainer kafka;
    private PostgreSQLContainer<?> postgres;

    @Override
    public Map<String, String> start() {
        this.kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));
        this.postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
        this.kafka.start();
        this.postgres.start();

        return Map.of(
                "kafka.bootstrap.servers", this.kafka.getBootstrapServers(),
                "quarkus.kafka.devservices.enabled", "false",
                "quarkus.datasource.jdbc.url", this.postgres.getJdbcUrl(),
                "quarkus.datasource.username", this.postgres.getUsername(),
                "quarkus.datasource.password", this.postgres.getPassword(),
                "quarkus.mailer.mock", "true"
        );
    }

    @Override
    public void stop() {
        if (this.kafka != null) {
            this.kafka.stop();
        }
        if (this.postgres != null) {
            this.postgres.stop();
        }
    }
}
