package io.github.flaviodotcom.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.flaviodotcom.config.WithKeycloakAndKafkaTestContainerProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@TestProfile(WithKeycloakAndKafkaTestContainerProfile.class)
class IdentityEventKafkaIT {

    private static final ObjectMapper JSON = new ObjectMapper();

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String bootstrapServers;

    @Test
    void givenIdentityEventsEnabled_WhenCreateUser_ThenPublishIdentityEvent() throws IOException {
        var username = "kafka.user." + UUID.randomUUID();
        var body = Map.of(
                "username", username,
                "email", username + "@example.com",
                "firstName", "Kafka",
                "lastName", "User",
                "enabled", true
        );

        given()
                .header("X-Actor-Id", "admin@example.com")
                .header("X-Correlation-Id", "correlation-identity-it")
                .contentType("application/json")
                .body(body)
                .when()
                .post("/v1/users")
                .then()
                .statusCode(201);

        var event = this.awaitEvent("identity.events", "identity.user.created", username);

        assertEquals("backend-keycloak", event.get("source").asText());
        assertEquals(1, event.get("schemaVersion").asInt());
        assertEquals("correlation-identity-it", event.get("correlationId").asText());
        assertEquals("admin@example.com", event.get("actor").get("id").asText());
        assertEquals("user", event.get("subject").get("type").asText());
        assertNotNull(event.get("subject").get("id").asText());
    }

    private JsonNode awaitEvent(String topic, String expectedEventType, String expectedUsername) throws IOException {
        try (var consumer = new KafkaConsumer<String, String>(this.consumerProperties())) {
            consumer.subscribe(List.of(topic));
            var deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();
            while (System.nanoTime() < deadline) {
                var records = consumer.poll(Duration.ofMillis(500));
                for (var record : records) {
                    var event = JSON.readTree(record.value());
                    if (expectedEventType.equals(event.get("eventType").asText())
                            && expectedUsername.equals(event.get("data").get("username").asText())) {
                        return event;
                    }
                }
            }
        }

        throw new AssertionError("Expected Kafka event was not published.");
    }

    private Properties consumerProperties() {
        var properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "identity-event-it-" + UUID.randomUUID());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return properties;
    }
}
