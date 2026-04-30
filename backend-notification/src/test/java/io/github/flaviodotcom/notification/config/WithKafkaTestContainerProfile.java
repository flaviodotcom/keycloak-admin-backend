package io.github.flaviodotcom.notification.config;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.List;

public class WithKafkaTestContainerProfile implements QuarkusTestProfile {

    @Override
    public List<TestResourceEntry> testResources() {
        return List.of(new TestResourceEntry(KafkaTestContainer.class));
    }
}
