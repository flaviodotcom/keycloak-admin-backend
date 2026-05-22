package io.github.flaviodotcom.audit.service;

import io.github.flaviodotcom.audit.dto.AuditEventResponse;
import io.github.flaviodotcom.audit.dto.pagination.PageRequest;
import io.github.flaviodotcom.audit.dto.pagination.PageResponse;

public interface AuditEventService {
    PageResponse<AuditEventResponse> findEvents(PageRequest pageRequest);

    AuditEventResponse findEventById(String eventId);

    void record(String topic, String payloadJson);
}
