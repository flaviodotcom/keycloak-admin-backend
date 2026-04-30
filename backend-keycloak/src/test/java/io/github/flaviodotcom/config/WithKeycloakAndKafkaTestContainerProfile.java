package io.github.flaviodotcom.config;

import io.github.flaviodotcom.config.kafka.KafkaTestContainer;
import io.github.flaviodotcom.containers.KeycloakTestContainer;
import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.List;
import java.util.Map;

public class WithKeycloakAndKafkaTestContainerProfile implements QuarkusTestProfile {

    @Override
    public List<TestResourceEntry> testResources() {
        return List.of(
                new TestResourceEntry(KeycloakTestContainer.class),
                new TestResourceEntry(KafkaTestContainer.class)
        );
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("identity.events.enabled", "true");
    }
}
