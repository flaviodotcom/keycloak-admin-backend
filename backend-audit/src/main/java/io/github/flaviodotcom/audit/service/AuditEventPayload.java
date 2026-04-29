package io.github.flaviodotcom.audit.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;

public record AuditEventPayload(JsonNode value) {

    public String requiredText(String fieldName) {
        var field = this.value.get(fieldName);
        if (field == null || field.isNull() || field.asText().isBlank()) {
            throw new IllegalArgumentException("Audit event field '%s' is required.".formatted(fieldName));
        }
        return field.asText();
    }

    public int requiredInt(String fieldName) {
        var field = this.value.get(fieldName);
        if (field == null || field.isNull() || !field.canConvertToInt()) {
            throw new IllegalArgumentException("Audit event field '%s' is required.".formatted(fieldName));
        }
        return field.asInt();
    }

    public String optionalText(String fieldName) {
        var field = this.value.get(fieldName);
        return field == null || field.isNull() || field.asText().isBlank() ? null : field.asText();
    }

    public String optionalNestedText(String objectName, String fieldName) {
        var object = this.value.get(objectName);
        if (object == null || object.isNull()) {
            return null;
        }
        return new AuditEventPayload(object).optionalText(fieldName);
    }

    public OffsetDateTime optionalOffsetDateTime(String fieldName) {
        var value = this.optionalText(fieldName);
        return value == null ? null : OffsetDateTime.parse(value);
    }
}
