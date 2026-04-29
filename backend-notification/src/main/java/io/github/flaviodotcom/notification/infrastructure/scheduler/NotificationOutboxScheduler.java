package io.github.flaviodotcom.notification.infrastructure.scheduler;

import io.github.flaviodotcom.notification.service.NotificationOutboxProcessor;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@ApplicationScoped
public class NotificationOutboxScheduler {

    private final NotificationOutboxProcessor processor;

    @Scheduled(every = "{notification.outbox.processing-interval}")
    void processPending() {
        var processed = this.processor.processPending();
        if (processed > 0) {
            log.info("Notification outbox processed entries={}", processed);
        }
    }
}
