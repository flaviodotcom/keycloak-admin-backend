package io.github.flaviodotcom.config;

import io.github.flaviodotcom.containers.KeycloakTestContainer;
import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.List;

public class WithKeycloakTestContainerProfile implements QuarkusTestProfile {

    @Override
    public List<TestResourceEntry> testResources() {
        return List.of(new TestResourceEntry(KeycloakTestContainer.class));
    }
}
