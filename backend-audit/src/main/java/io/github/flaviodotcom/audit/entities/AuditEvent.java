package io.github.flaviodotcom.audit.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_events")
public class AuditEvent extends PanacheEntity {

    @Column(name = "event_id", nullable = false, unique = true, updatable = false)
    public String eventId;

    @Column(name = "schema_version", nullable = false, updatable = false)
    public int schemaVersion;

    @Column(name = "topic", nullable = false, updatable = false)
    public String topic;

    @Column(name = "event_type", nullable = false, updatable = false)
    public String eventType;

    @Column(name = "source", updatable = false)
    public String source;

    @Column(name = "correlation_id", updatable = false)
    public String correlationId;

    @Column(name = "actor_id", updatable = false)
    public String actorId;

    @Column(name = "subject_type", updatable = false)
    public String subjectType;

    @Column(name = "subject_id", updatable = false)
    public String subjectId;

    @Column(name = "occurred_at", updatable = false)
    public OffsetDateTime occurredAt;

    @Column(name = "received_at", nullable = false, updatable = false)
    public OffsetDateTime receivedAt;

    @Column(name = "payload_json", nullable = false, updatable = false, columnDefinition = "TEXT")
    public String payloadJson;
}
