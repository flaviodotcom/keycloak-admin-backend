package io.github.flaviodotcom.audit.repository;

import io.github.flaviodotcom.audit.entities.AuditEvent;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class AuditEventRepository implements PanacheRepository<AuditEvent> {

    public boolean existsByEventId(String eventId) {
        return count("eventId", eventId) > 0;
    }

    public Optional<AuditEvent> findByEventId(String eventId) {
        return find("eventId", eventId).firstResultOptional();
    }
}
