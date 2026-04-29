package io.github.flaviodotcom.audit.service;

import io.github.flaviodotcom.audit.repository.AuditEventRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@ApplicationScoped
public class AuditEventService {

    private final AuditEventRepository repository;
    private final AuditEventPayloadParser payloadParser;
    private final AuditEventMapper eventMapper;

    @Transactional
    public void record(String topic, String payloadJson) {
        var payload = this.payloadParser.parse(payloadJson);
        var eventId = payload.requiredText("eventId");

        if (this.repository.existsByEventId(eventId)) {
            log.info("Audit event already recorded eventId={} topic={}", eventId, topic);
            return;
        }

        var event = this.eventMapper.toEntity(topic, payloadJson, payload);
        this.repository.persist(event);
        log.info(
                "Audit event persisted eventId={} eventType={} correlationId={} actor={} subjectType={} subjectId={} topic={}",
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
