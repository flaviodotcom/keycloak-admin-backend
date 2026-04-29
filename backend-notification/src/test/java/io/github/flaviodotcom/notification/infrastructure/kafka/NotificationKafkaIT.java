package io.github.flaviodotcom.notification.infrastructure.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.flaviodotcom.notification.config.WithKafkaTestContainerProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestProfile(WithKafkaTestContainerProfile.class)
class NotificationKafkaIT {

    private static final ObjectMapper JSON = new ObjectMapper();

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String bootstrapServers;

    @Test
    void givenNotificationCommand_WhenConsumed_ThenPublishSentEvent() throws Exception {
        var commandId = UUID.randomUUID().toString();
        var command = Map.of(
                "commandId", commandId,
                "schemaVersion", 1,
                "correlationId", "correlation-notification-it",
                "requestedBy", "admin@example.com",
                "to", List.of("user@example.com"),
                "subject", "Update your password",
                "textBody", "Hello, update your password.",
                "metadata", Map.of("notificationType", "update-password")
        );

        this.produce("notification.commands", commandId, JSON.writeValueAsString(command));
        var event = this.awaitNotificationEvent(commandId);

        assertEquals("notification.email.sent", event.get("eventType").asText());
        assertEquals(1, event.get("schemaVersion").asInt());
        assertEquals("backend-notification", event.get("source").asText());
        assertEquals("correlation-notification-it", event.get("correlationId").asText());
        assertEquals("admin@example.com", event.get("actor").get("id").asText());
        assertEquals("user@example.com", event.get("recipients").get(0).asText());
    }

    @Test
    void givenDuplicateNotificationCommand_WhenConsumed_ThenSendOnlyOnce() throws Exception {
        var commandId = UUID.randomUUID().toString();
        var command = Map.of(
                "commandId", commandId,
                "schemaVersion", 1,
                "correlationId", "correlation-duplicate-it",
                "requestedBy", "admin@example.com",
                "to", List.of("duplicate@example.com"),
                "subject", "Update your password",
                "textBody", "Hello, update your password.",
                "metadata", Map.of("notificationType", "update-password")
        );

        var payload = JSON.writeValueAsString(command);
        this.produce("notification.commands", commandId, payload);
        this.produce("notification.commands", commandId, payload);

        assertEquals(1, this.countNotificationEvents(commandId, Duration.ofSeconds(5)));
    }

    @Test
    void givenNotificationCommandWithoutBody_WhenConsumed_ThenPublishFailedEventAndSendCommandToDlq() throws Exception {
        var commandId = UUID.randomUUID().toString();
        var command = Map.of(
                "commandId", commandId,
                "schemaVersion", 1,
                "correlationId", "correlation-failed-it",
                "requestedBy", "admin@example.com",
                "to", List.of("user@example.com"),
                "subject", "Update your password",
                "metadata", Map.of("notificationType", "update-password")
        );

        this.produce("notification.commands", commandId, JSON.writeValueAsString(command));

        var event = this.awaitNotificationEvent(commandId);
        var dlqPayload = this.awaitRecord("notification.commands.dlq", commandId);

        assertEquals("notification.email.failed", event.get("eventType").asText());
        assertEquals("correlation-failed-it", event.get("correlationId").asText());
        assertEquals(commandId, JSON.readTree(dlqPayload).get("commandId").asText());
    }

    @Test
    void givenInvalidNotificationCommandPayload_WhenConsumed_ThenSendCommandToDlq() {
        var commandId = UUID.randomUUID().toString();
        var payload = "{\"commandId\":\"" + commandId + "\",\"requestedBy\":\"admin@example.com\"}";

        this.produce("notification.commands", commandId, payload);

        assertEquals(payload, this.awaitRecord("notification.commands.dlq", commandId));
    }

    @Test
    void givenKafkaPostgresAndMailerAvailable_WhenReadinessIsRequested_ThenReturnUp() {
        given()
                .when()
                .get("/q/health/ready")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    private JsonNode awaitNotificationEvent(String commandId) throws Exception {
        try (var consumer = new KafkaConsumer<String, String>(this.consumerProperties())) {
            consumer.subscribe(List.of("notification.events"));
            var deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();

            while (System.nanoTime() < deadline) {
                var records = consumer.poll(Duration.ofMillis(500));
                for (var record : records) {
                    var event = JSON.readTree(record.value());
                    if (commandId.equals(event.get("commandId").asText())) {
                        return event;
                    }
                }
            }
        }

        throw new AssertionError("Expected notification event was not published.");
    }

    private int countNotificationEvents(String commandId, Duration duration) throws Exception {
        var count = 0;
        try (var consumer = new KafkaConsumer<String, String>(this.consumerProperties())) {
            consumer.subscribe(List.of("notification.events"));
            var deadline = System.nanoTime() + duration.toNanos();

            while (System.nanoTime() < deadline) {
                var records = consumer.poll(Duration.ofMillis(500));
                for (var record : records) {
                    var event = JSON.readTree(record.value());
                    if (commandId.equals(event.get("commandId").asText())) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private String awaitRecord(String topic, String key) {
        try (var consumer = new KafkaConsumer<String, String>(this.consumerProperties())) {
            consumer.subscribe(List.of(topic));
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
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-event-it-" + UUID.randomUUID());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return properties;
    }
}
