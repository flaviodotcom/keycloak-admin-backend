package io.github.flaviodotcom.notification.service.impl;

import io.github.flaviodotcom.notification.config.properties.NotificationOutboxProperties;
import io.github.flaviodotcom.notification.domain.factory.NotificationEventFactory;
import io.github.flaviodotcom.notification.domain.gateway.EmailGateway;
import io.github.flaviodotcom.notification.domain.gateway.NotificationEventGateway;
import io.github.flaviodotcom.notification.domain.model.NotificationOutboxItem;
import io.github.flaviodotcom.notification.dto.EmailNotificationCommand;
import io.github.flaviodotcom.notification.dto.NotificationEvent;
import io.github.flaviodotcom.notification.service.NotificationOutboxService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationOutboxProcessorImplTest {

    @Test
    void givenEmailGatewayFailureBeforeMaxAttempts_WhenProcessingOutbox_ThenScheduleRetryAndPropagateError() {
        var command = this.command();
        var outboxService = new CapturingOutboxService(command, 1);
        var eventGateway = new CapturingNotificationEventGateway();
        EmailGateway emailGateway = ignored -> {
            throw new IllegalStateException("SMTP is unavailable.");
        };
        var processor = new NotificationOutboxProcessorImpl(
                emailGateway,
                eventGateway,
                new NotificationEventFactory(),
                outboxService,
                this.properties()
        );

        var exception = assertThrows(IllegalStateException.class, processor::processPending);

        assertEquals("SMTP is unavailable.", exception.getMessage());
        assertEquals(1L, outboxService.retryableFailureOutboxId);
        assertEquals(command.commandId(), outboxService.retryableFailureCommandId);
        assertEquals("SMTP is unavailable.", outboxService.retryableFailureErrorMessage);
        assertEquals(Duration.ofSeconds(30), outboxService.retryDelay);
        assertEquals(0, eventGateway.events.size());
    }

    @Test
    void givenEmailGatewayFailureOnMaxAttempts_WhenProcessingOutbox_ThenMarkFailedPublishEventAndPropagateError() {
        var command = this.command();
        var outboxService = new CapturingOutboxService(command, 3);
        var eventGateway = new CapturingNotificationEventGateway();
        EmailGateway emailGateway = ignored -> {
            throw new IllegalStateException("SMTP is unavailable.");
        };
        var processor = new NotificationOutboxProcessorImpl(
                emailGateway,
                eventGateway,
                new NotificationEventFactory(),
                outboxService,
                this.properties()
        );

        var exception = assertThrows(IllegalStateException.class, processor::processPending);

        assertEquals("SMTP is unavailable.", exception.getMessage());
        assertEquals(1L, outboxService.failedOutboxId);
        assertEquals(command.commandId(), outboxService.failedCommandId);
        assertEquals("SMTP is unavailable.", outboxService.failedErrorMessage);
        assertEquals("notification.email.failed", eventGateway.events.getFirst().eventType());
        assertEquals(command.commandId(), eventGateway.events.getFirst().commandId());
    }

    private EmailNotificationCommand command() {
        return new EmailNotificationCommand(
                "command-1",
                1,
                "correlation-1",
                "admin@example.com",
                null,
                List.of("user@example.com"),
                null,
                null,
                "Update your password",
                "Hello.",
                null,
                null,
                Map.of("notificationType", "update-password")
        );
    }

    private NotificationOutboxProperties properties() {
        return new NotificationOutboxProperties() {

            @Override
            public int batchSize() {
                return 10;
            }

            @Override
            public String processingInterval() {
                return "2s";
            }

            @Override
            public int maxAttempts() {
                return 3;
            }

            @Override
            public Duration retryDelay() {
                return Duration.ofSeconds(30);
            }
        };
    }

    private static class CapturingOutboxService implements NotificationOutboxService {

        private final EmailNotificationCommand command;
        private final int attempt;
        private Long retryableFailureOutboxId;
        private String retryableFailureCommandId;
        private String retryableFailureErrorMessage;
        private Duration retryDelay;
        private Long failedOutboxId;
        private String failedCommandId;
        private String failedErrorMessage;

        private CapturingOutboxService(EmailNotificationCommand command, int attempt) {
            this.command = command;
            this.attempt = attempt;
        }

        @Override
        public boolean enqueue(EmailNotificationCommand command) {
            throw new UnsupportedOperationException("This test only processes pending outbox entries.");
        }

        @Override
        public List<NotificationOutboxItem> claimPending(int batchSize) {
            return List.of(new NotificationOutboxItem(1L, this.attempt, this.command));
        }

        @Override
        public void markSent(Long outboxId, String commandId) {
            throw new UnsupportedOperationException("This test covers the failure path.");
        }

        @Override
        public void markRetryableFailure(Long outboxId, String commandId, String errorMessage, Duration retryDelay) {
            this.retryableFailureOutboxId = outboxId;
            this.retryableFailureCommandId = commandId;
            this.retryableFailureErrorMessage = errorMessage;
            this.retryDelay = retryDelay;
        }

        @Override
        public void markFailed(Long outboxId, String commandId, String errorMessage) {
            this.failedOutboxId = outboxId;
            this.failedCommandId = commandId;
            this.failedErrorMessage = errorMessage;
        }
    }

    private static class CapturingNotificationEventGateway implements NotificationEventGateway {

        private final List<NotificationEvent> events = new ArrayList<>();

        @Override
        public void publish(NotificationEvent event) {
            this.events.add(event);
        }
    }
}
