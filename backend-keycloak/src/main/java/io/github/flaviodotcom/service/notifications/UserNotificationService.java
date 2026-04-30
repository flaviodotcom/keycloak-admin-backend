package io.github.flaviodotcom.service.notifications;

import io.github.flaviodotcom.config.properties.NotificationProperties;
import io.github.flaviodotcom.domain.identity.gateway.IdentityUserActionGateway;
import io.github.flaviodotcom.domain.identity.gateway.IdentityUserGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityUser;
import io.github.flaviodotcom.exceptions.BusinessException;
import io.github.flaviodotcom.service.events.RequestActorResolver;
import io.github.flaviodotcom.service.events.RequestCorrelationIdResolver;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@ApplicationScoped
public class UserNotificationService {

    private static final String DISPLAY_NAME_TOKEN = "%DISPLAY_NAME%";

    private final IdentityUserGateway identityUserGateway;
    private final IdentityUserActionGateway identityUserActionGateway;
    private final NotificationCommandPublisher notificationCommandPublisher;
    private final RequestActorResolver actorResolver;
    private final RequestCorrelationIdResolver correlationIdResolver;
    private final NotificationProperties notificationProperties;

    public void sendUpdatePasswordEmail(String userId) {
        if (!this.notificationProperties.commands().enabled()) {
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
                this.correlationIdResolver.resolve(),
                this.actorResolver.resolve(),
                null,
                List.of(user.email()),
                List.of(),
                List.of(),
                this.notificationProperties.updatePassword().subject(),
                this.notificationProperties.updatePassword().textBody().replace(DISPLAY_NAME_TOKEN, this.displayName(user)),
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
