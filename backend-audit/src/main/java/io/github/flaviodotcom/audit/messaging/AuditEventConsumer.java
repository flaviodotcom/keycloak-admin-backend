package io.github.flaviodotcom.audit.messaging;

import io.github.flaviodotcom.audit.service.AuditEventService;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class AuditEventConsumer {

    private static final String IDENTITY_EVENTS_TOPIC = "identity.events";
    private static final String NOTIFICATION_EVENTS_TOPIC = "notification.events";

    private final AuditEventService auditEventService;

    public AuditEventConsumer(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @Incoming("identity-events")
    @Blocking
    public void consumeIdentityEvent(String payloadJson) {
        this.auditEventService.record(IDENTITY_EVENTS_TOPIC, payloadJson);
    }

    @Incoming("notification-events")
    @Blocking
    public void consumeNotificationEvent(String payloadJson) {
        this.auditEventService.record(NOTIFICATION_EVENTS_TOPIC, payloadJson);
    }
}
