package io.github.flaviodotcom.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuditEventPayloadParser {

    private final ObjectMapper objectMapper;

    public AuditEventPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AuditEventPayload parse(String payloadJson) {
        try {
            return new AuditEventPayload(this.objectMapper.readTree(payloadJson));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Audit event payload must be valid JSON.", exception);
        }
    }
}
