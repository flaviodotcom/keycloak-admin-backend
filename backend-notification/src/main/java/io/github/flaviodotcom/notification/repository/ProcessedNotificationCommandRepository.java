package io.github.flaviodotcom.notification.repository;

import io.github.flaviodotcom.notification.entity.ProcessedNotificationCommand;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class ProcessedNotificationCommandRepository implements PanacheRepository<ProcessedNotificationCommand> {

    public Optional<ProcessedNotificationCommand> findByCommandId(String commandId) {
        return find("commandId", commandId).firstResultOptional();
    }
}
