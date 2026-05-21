package io.github.flaviodotcom.config;

import io.github.flaviodotcom.config.restassured.ActorHeaderFilter;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public abstract class AbstractIntegrationTest {

    @BeforeAll
    static void setupRestAssured() {
        RestAssured.filters(new ActorHeaderFilter());
    }
}
