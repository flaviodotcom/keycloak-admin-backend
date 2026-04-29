package io.github.flaviodotcom.audit.service;

import io.github.flaviodotcom.audit.repository.AuditEventRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AuditEventService {

    private static final Logger LOG = Logger.getLogger(AuditEventService.class);

    private final AuditEventRepository repository;
    private final AuditEventPayloadParser payloadParser;
    private final AuditEventMapper eventMapper;

    public AuditEventService(
            AuditEventRepository repository,
            AuditEventPayloadParser payloadParser,
            AuditEventMapper eventMapper
    ) {
        this.repository = repository;
        this.payloadParser = payloadParser;
        this.eventMapper = eventMapper;
    }

    @Transactional
    public void record(String topic, String payloadJson) {
        var payload = this.payloadParser.parse(payloadJson);
        var eventId = payload.requiredText("eventId");

        if (this.repository.existsByEventId(eventId)) {
            LOG.infof("Audit event already recorded eventId=%s topic=%s", eventId, topic);
            return;
        }

        var event = this.eventMapper.toEntity(topic, payloadJson, payload);
        this.repository.persist(event);
        LOG.infof(
                "Audit event persisted eventId=%s eventType=%s correlationId=%s actor=%s subjectType=%s subjectId=%s topic=%s",
                event.eventId,
                event.eventType,
                event.correlationId,
                event.actorId,
                event.subjectType,
                event.subjectId,
                event.topic
        );
    }
}
