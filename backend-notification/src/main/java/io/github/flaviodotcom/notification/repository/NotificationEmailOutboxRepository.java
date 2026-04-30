package io.github.flaviodotcom.notification.repository;

import io.github.flaviodotcom.notification.domain.model.NotificationOutboxStatus;
import io.github.flaviodotcom.notification.entities.NotificationEmailOutbox;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.time.OffsetDateTime;

@ApplicationScoped
public class NotificationEmailOutboxRepository implements PanacheRepository<NotificationEmailOutbox> {

    public Optional<NotificationEmailOutbox> findByCommandId(String commandId) {
        return find("commandId", commandId).firstResultOptional();
    }

    @SuppressWarnings("unchecked")
    public List<NotificationEmailOutbox> findPendingForUpdate(int batchSize, OffsetDateTime now) {
        return getEntityManager()
                .createNativeQuery("""
                        SELECT *
                        FROM notification_email_outbox
                        WHERE status = :status
                        AND (next_attempt_at IS NULL OR next_attempt_at <= :now)
                        ORDER BY created_at
                        LIMIT :batchSize
                        FOR UPDATE SKIP LOCKED
                        """, NotificationEmailOutbox.class)
                .setParameter("status", NotificationOutboxStatus.PENDING.name())
                .setParameter("now", now)
                .setParameter("batchSize", batchSize)
                .getResultList();
    }
}
