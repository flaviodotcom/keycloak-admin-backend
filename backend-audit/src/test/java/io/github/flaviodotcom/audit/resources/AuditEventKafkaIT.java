package io.github.flaviodotcom.audit.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.flaviodotcom.audit.config.WithKafkaAndPostgresTestContainerProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@TestProfile(WithKafkaAndPostgresTestContainerProfile.class)
class AuditEventKafkaIT {

    private static final ObjectMapper JSON = new ObjectMapper();

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String bootstrapServers;

    @Test
    void givenIdentityEvent_WhenConsumed_ThenPersistAuditRecord() throws Exception {
        var eventId = UUID.randomUUID().toString();
        var event = Map.of(
                "eventId", eventId,
                "schemaVersion", 1,
                "eventType", "identity.user.created",
                "source", "backend-keycloak",
                "actor", Map.of("id", "admin@example.com"),
                "subject", Map.of("type", "user", "id", "user-1"),
                "occurredAt", OffsetDateTime.now().toString(),
                "data", Map.of("username", "audit.user")
        );

        this.produce("identity.events", eventId, JSON.writeValueAsString(event));

        this.awaitAuditEvent(eventId);
    }

    private void awaitAuditEvent(String eventId) throws InterruptedException {
        var deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();
        AssertionError lastError = null;

        while (System.nanoTime() < deadline) {
            try {
                given()
                        .when()
                        .get("/v1/audit-events/{eventId}", eventId)
                        .then()
                        .statusCode(200)
                        .body("eventId", equalTo(eventId))
                        .body("schemaVersion", equalTo(1))
                        .body("topic", equalTo("identity.events"))
                        .body("eventType", equalTo("identity.user.created"))
                        .body("actorId", equalTo("admin@example.com"))
                        .body("subjectType", equalTo("user"))
                        .body("subjectId", equalTo("user-1"));
                return;
            } catch (AssertionError error) {
                lastError = error;
                Thread.sleep(500);
            }
        }

        throw lastError == null ? new AssertionError("Audit event was not persisted.") : lastError;
    }

    private void produce(String topic, String key, String value) {
        try (var producer = new KafkaProducer<String, String>(this.producerProperties())) {
            producer.send(new ProducerRecord<>(topic, key, value)).get();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not produce Kafka test record.", exception);
        }
    }

    private Properties producerProperties() {
        var properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        return properties;
    }
}
