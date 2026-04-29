package io.github.flaviodotcom.notification.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notification_email_outbox")
public class NotificationEmailOutbox extends PanacheEntity {

    @Column(name = "command_id", nullable = false, unique = true, updatable = false)
    public String commandId;

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    public String payloadJson;

    @Column(name = "status", nullable = false)
    public String status;

    @Column(name = "attempts", nullable = false)
    public int attempts;

    @Column(name = "created_at", nullable = false, updatable = false)
    public OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public OffsetDateTime updatedAt;

    @Column(name = "processed_at")
    public OffsetDateTime processedAt;

    @Column(name = "next_attempt_at")
    public OffsetDateTime nextAttemptAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    public String errorMessage;
}
