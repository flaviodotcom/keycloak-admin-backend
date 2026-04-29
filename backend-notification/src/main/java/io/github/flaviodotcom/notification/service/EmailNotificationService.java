package io.github.flaviodotcom.notification.service;

import io.github.flaviodotcom.notification.dto.EmailNotificationCommand;
import io.github.flaviodotcom.notification.messaging.NotificationEventPublisher;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmailNotificationService {

    private final Mailer mailer;
    private final NotificationEventPublisher eventPublisher;
    private final NotificationCommandValidator commandValidator;
    private final EmailMessageFactory messageFactory;
    private final NotificationEventFactory eventFactory;

    public EmailNotificationService(
            Mailer mailer,
            NotificationEventPublisher eventPublisher,
            NotificationCommandValidator commandValidator,
            EmailMessageFactory messageFactory,
            NotificationEventFactory eventFactory
    ) {
        this.mailer = mailer;
        this.eventPublisher = eventPublisher;
        this.commandValidator = commandValidator;
        this.messageFactory = messageFactory;
        this.eventFactory = eventFactory;
    }

    public void send(EmailNotificationCommand command) {
        this.commandValidator.validate(command);

        try {
            this.mailer.send(this.messageFactory.create(command));
            this.eventPublisher.publish(this.eventFactory.sent(command));
        } catch (RuntimeException exception) {
            this.eventPublisher.publish(this.eventFactory.failed(command, exception.getMessage()));
            throw exception;
        }
    }
}
