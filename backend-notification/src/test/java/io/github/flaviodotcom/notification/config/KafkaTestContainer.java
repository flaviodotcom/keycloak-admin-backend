package io.github.flaviodotcom.notification.config;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class KafkaTestContainer implements QuarkusTestResourceLifecycleManager {

    private KafkaContainer kafka;

    @Override
    public Map<String, String> start() {
        this.kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));
        this.kafka.start();

        return Map.of(
                "kafka.bootstrap.servers", this.kafka.getBootstrapServers(),
                "quarkus.kafka.devservices.enabled", "false",
                "quarkus.mailer.mock", "true"
        );
    }

    @Override
    public void stop() {
        if (this.kafka != null) {
            this.kafka.stop();
        }
    }
}
