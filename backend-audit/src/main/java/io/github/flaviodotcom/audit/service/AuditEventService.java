package io.github.flaviodotcom.audit.service;

import io.github.flaviodotcom.audit.repository.AuditEventRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuditEventService {

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
            return;
        }

        this.repository.persist(this.eventMapper.toEntity(topic, payloadJson, payload));
    }
}
