package io.github.flaviodotcom.audit.config;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.List;
import java.util.Map;

public class WithKafkaAndPostgresTestContainerProfile implements QuarkusTestProfile {

    @Override
    public List<TestResourceEntry> testResources() {
        return List.of(new TestResourceEntry(KafkaAndPostgresTestContainer.class));
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("quarkus.datasource.devservices.enabled", "false");
    }
}
