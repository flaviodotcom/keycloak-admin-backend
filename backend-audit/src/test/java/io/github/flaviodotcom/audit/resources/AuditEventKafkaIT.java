package io.github.flaviodotcom.audit.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.flaviodotcom.audit.config.WithKafkaAndPostgresTestContainerProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
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
                "correlationId", "correlation-audit-identity-it",
                "actor", Map.of("id", "admin@example.com"),
                "subject", Map.of("type", "user", "id", "user-1"),
                "occurredAt", OffsetDateTime.now().toString(),
                "data", Map.of("username", "audit.user")
        );

        this.produce("identity.events", eventId, JSON.writeValueAsString(event));

        this.awaitAuditEvent(
                eventId,
                "identity.events",
                "identity.user.created",
                "correlation-audit-identity-it",
                "admin@example.com",
                "user",
                "user-1"
        );
    }

    @Test
    void givenNotificationEvent_WhenConsumed_ThenPersistAuditRecord() throws Exception {
        var eventId = UUID.randomUUID().toString();
        var event = Map.of(
                "eventId", eventId,
                "schemaVersion", 1,
                "eventType", "notification.email.sent",
                "source", "backend-notification",
                "commandId", "command-1",
                "correlationId", "correlation-audit-notification-it",
                "actor", Map.of("id", "admin@example.com"),
                "recipients", java.util.List.of("user@example.com"),
                "occurredAt", OffsetDateTime.now().toString(),
                "metadata", Map.of("notificationType", "update-password")
        );

        this.produce("notification.events", eventId, JSON.writeValueAsString(event));

        this.awaitAuditEvent(
                eventId,
                "notification.events",
                "notification.email.sent",
                "correlation-audit-notification-it",
                "admin@example.com",
                null,
                null
        );
    }

    @Test
    void givenInvalidIdentityEventPayload_WhenConsumed_ThenSendEventToDlq() {
        var eventId = UUID.randomUUID().toString();
        var payload = "{\"eventId\":\"" + eventId + "\"}";

        this.produce("identity.events", eventId, payload);

        org.junit.jupiter.api.Assertions.assertEquals(payload, this.awaitRecord("identity.events.audit.dlq", eventId));
    }

    @Test
    void givenKafkaAndPostgresAvailable_WhenReadinessIsRequested_ThenReturnUp() {
        given()
                .when()
                .get("/q/health/ready")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    private void awaitAuditEvent(String eventId,
                                 String topic,
                                 String eventType,
                                 String correlationId,
                                 String actorId,
                                 String subjectType,
                                 String subjectId) throws InterruptedException {
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
                        .body("topic", equalTo(topic))
                        .body("eventType", equalTo(eventType))
                        .body("correlationId", equalTo(correlationId))
                        .body("actorId", equalTo(actorId))
                        .body("subjectType", equalTo(subjectType))
                        .body("subjectId", equalTo(subjectId));
                return;
            } catch (AssertionError error) {
                lastError = error;
                Thread.sleep(500);
            }
        }

        throw lastError == null ? new AssertionError("Audit event was not persisted.") : lastError;
    }

    private String awaitRecord(String topic, String key) {
        try (var consumer = new KafkaConsumer<String, String>(this.consumerProperties())) {
            consumer.subscribe(java.util.List.of(topic));
            var deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();

            while (System.nanoTime() < deadline) {
                var records = consumer.poll(Duration.ofMillis(500));
                for (var record : records) {
                    if (key.equals(record.key())) {
                        return record.value();
                    }
                }
            }
        }

        throw new AssertionError("Expected Kafka record was not published to " + topic + ".");
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

    private Properties consumerProperties() {
        var properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "audit-event-it-" + UUID.randomUUID());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return properties;
    }
}
