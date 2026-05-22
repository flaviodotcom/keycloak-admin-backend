package io.github.flaviodotcom.audit.service.impl;

import io.github.flaviodotcom.audit.dto.AuditEventFilter;
import io.github.flaviodotcom.audit.dto.AuditEventResponse;
import io.github.flaviodotcom.audit.dto.pagination.PageRequest;
import io.github.flaviodotcom.audit.dto.pagination.PageResponse;
import io.github.flaviodotcom.audit.dto.pagination.SortDirection;
import io.github.flaviodotcom.audit.repository.AuditEventRepository;
import io.github.flaviodotcom.audit.service.AuditEventMapper;
import io.github.flaviodotcom.audit.service.AuditEventPayloadParser;
import io.github.flaviodotcom.audit.service.AuditEventService;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@ApplicationScoped
public class AuditEventServiceImpl implements AuditEventService {

    private final AuditEventRepository repository;
    private final AuditEventPayloadParser payloadParser;
    private final AuditEventMapper eventMapper;

    @Override
    public PageResponse<AuditEventResponse> findEvents(AuditEventFilter filter, PageRequest pageRequest) {
        var sort = Sort.by(
                pageRequest.sortBy(),
                pageRequest.sort() == SortDirection.ASC
                        ? Sort.Direction.Ascending
                        : Sort.Direction.Descending
        );

        return PageResponse.from(
                repository.findByFilter(filter, sort),
                pageRequest
        ).map(AuditEventResponse::fromEntity);
    }

    @Override
    public AuditEventResponse findEventById(String eventId) {
        return this.repository.findByEventId(eventId)
                .map(AuditEventResponse::fromEntity)
                .orElseThrow(NotFoundException::new);
    }

    @Override
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
