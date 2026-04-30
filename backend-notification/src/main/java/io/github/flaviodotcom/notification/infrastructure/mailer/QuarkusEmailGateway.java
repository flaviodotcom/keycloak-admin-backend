package io.github.flaviodotcom.notification.infrastructure.mailer;

import io.github.flaviodotcom.notification.domain.gateway.EmailGateway;
import io.github.flaviodotcom.notification.dto.EmailNotificationCommand;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@ApplicationScoped
public class QuarkusEmailGateway implements EmailGateway {

    private final Mailer mailer;
    private final EmailMessageFactory messageFactory;

    @Override
    public void send(EmailNotificationCommand command) {
        this.mailer.send(this.messageFactory.create(command));
    }
}
