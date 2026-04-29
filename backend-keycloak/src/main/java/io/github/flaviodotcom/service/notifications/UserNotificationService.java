package io.github.flaviodotcom.service.notifications;

import io.github.flaviodotcom.domain.identity.gateway.IdentityUserActionGateway;
import io.github.flaviodotcom.domain.identity.gateway.IdentityUserGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityUser;
import io.github.flaviodotcom.exceptions.BusinessException;
import io.github.flaviodotcom.service.events.RequestActorResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class UserNotificationService {

    private final IdentityUserGateway identityUserGateway;
    private final IdentityUserActionGateway identityUserActionGateway;
    private final NotificationCommandPublisher notificationCommandPublisher;
    private final RequestActorResolver actorResolver;

    @ConfigProperty(name = "notification.commands.enabled")
    boolean notificationCommandsEnabled;

    @ConfigProperty(name = "notification.update-password.subject")
    String updatePasswordSubject;

    @ConfigProperty(name = "notification.update-password.text-body")
    String updatePasswordTextBody;

    @Inject
    public UserNotificationService(IdentityUserGateway identityUserGateway,
                                   IdentityUserActionGateway identityUserActionGateway,
                                   NotificationCommandPublisher notificationCommandPublisher,
                                   RequestActorResolver actorResolver) {
        this.identityUserGateway = identityUserGateway;
        this.identityUserActionGateway = identityUserActionGateway;
        this.notificationCommandPublisher = notificationCommandPublisher;
        this.actorResolver = actorResolver;
    }

    public void sendUpdatePasswordEmail(String userId) {
        if (!this.notificationCommandsEnabled) {
            this.identityUserActionGateway.sendUpdatePasswordEmail(userId);
            return;
        }

        var user = this.identityUserGateway.findUserById(userId);
        this.requireEmail(user);
        this.notificationCommandPublisher.publish(this.toUpdatePasswordCommand(user));
    }

    private EmailNotificationCommand toUpdatePasswordCommand(IdentityUser user) {
        return new EmailNotificationCommand(
                UUID.randomUUID().toString(),
                1,
                this.actorResolver.resolve(),
                null,
                List.of(user.email()),
                List.of(),
                List.of(),
                this.updatePasswordSubject,
                MessageFormat.format(this.updatePasswordTextBody, this.displayName(user)),
                null,
                List.of(),
                Map.of(
                        "notificationType", "update-password",
                        "userId", user.id(),
                        "username", user.username()
                )
        );
    }

    private void requireEmail(IdentityUser user) {
        if (user.email() == null || user.email().isBlank()) {
            throw BusinessException.localized("error.notification.user-email-required", user.id());
        }
    }

    private String displayName(IdentityUser user) {
        if (user.firstName() != null && !user.firstName().isBlank()) {
            return user.firstName();
        }
        return user.username();
    }
}
