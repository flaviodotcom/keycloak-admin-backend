package io.github.flaviodotcom.notification.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "processed_notification_commands")
public class ProcessedNotificationCommand extends PanacheEntity {

    @Column(name = "command_id", nullable = false, unique = true, updatable = false)
    public String commandId;

    @Column(name = "status", nullable = false)
    public String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    public OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public OffsetDateTime updatedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    public String errorMessage;
}
