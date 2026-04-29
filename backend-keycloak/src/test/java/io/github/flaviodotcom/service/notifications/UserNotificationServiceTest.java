package io.github.flaviodotcom.service.notifications;

import io.github.flaviodotcom.domain.identity.gateway.IdentityUserActionGateway;
import io.github.flaviodotcom.domain.identity.gateway.IdentityUserGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityUser;
import io.github.flaviodotcom.exceptions.BusinessException;
import io.github.flaviodotcom.service.events.RequestActorResolver;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserNotificationServiceTest {

    @Test
    void givenNotificationCommandsDisabled_WhenSendUpdatePasswordEmail_ThenUseKeycloakActionEmail() {
        var identityUserGateway = mock(IdentityUserGateway.class);
        var identityUserActionGateway = mock(IdentityUserActionGateway.class);
        var notificationCommandPublisher = mock(NotificationCommandPublisher.class);
        var actorResolver = mock(RequestActorResolver.class);
        var service = service(identityUserGateway, identityUserActionGateway, notificationCommandPublisher, actorResolver);
        service.notificationCommandsEnabled = false;

        service.sendUpdatePasswordEmail("user-1");

        verify(identityUserActionGateway).sendUpdatePasswordEmail("user-1");
        verify(notificationCommandPublisher, never()).publish(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void givenNotificationCommandsEnabled_WhenSendUpdatePasswordEmail_ThenPublishNotificationCommand() {
        var identityUserGateway = mock(IdentityUserGateway.class);
        var identityUserActionGateway = mock(IdentityUserActionGateway.class);
        var notificationCommandPublisher = mock(NotificationCommandPublisher.class);
        var actorResolver = mock(RequestActorResolver.class);
        var service = service(identityUserGateway, identityUserActionGateway, notificationCommandPublisher, actorResolver);
        service.notificationCommandsEnabled = true;
        service.updatePasswordSubject = "Update your password";
        service.updatePasswordTextBody = "Hello {0}, update your password.";

        when(identityUserGateway.findUserById("user-1")).thenReturn(user("user-1", "maria.teste", "maria@example.com", "Maria"));
        when(actorResolver.resolve()).thenReturn("admin@example.com");

        service.sendUpdatePasswordEmail("user-1");

        verify(identityUserActionGateway, never()).sendUpdatePasswordEmail("user-1");
        verify(notificationCommandPublisher).publish(argThat(command ->
                command.requestedBy().equals("admin@example.com")
                        && command.schemaVersion() == 1
                        && command.to().contains("maria@example.com")
                        && command.subject().equals("Update your password")
                        && command.textBody().equals("Hello Maria, update your password.")
                        && command.metadata().get("notificationType").equals("update-password")
        ));
    }

    @Test
    void givenNotificationCommandsEnabledAndUserWithoutEmail_WhenSendUpdatePasswordEmail_ThenThrowBusinessException() {
        var identityUserGateway = mock(IdentityUserGateway.class);
        var identityUserActionGateway = mock(IdentityUserActionGateway.class);
        var notificationCommandPublisher = mock(NotificationCommandPublisher.class);
        var actorResolver = mock(RequestActorResolver.class);
        var service = service(identityUserGateway, identityUserActionGateway, notificationCommandPublisher, actorResolver);
        service.notificationCommandsEnabled = true;

        when(identityUserGateway.findUserById("user-1")).thenReturn(user("user-1", "maria.teste", null, "Maria"));

        assertThrows(BusinessException.class, () -> service.sendUpdatePasswordEmail("user-1"));

        verify(identityUserActionGateway, never()).sendUpdatePasswordEmail("user-1");
        verify(notificationCommandPublisher, never()).publish(org.mockito.ArgumentMatchers.any());
    }

    private static UserNotificationService service(IdentityUserGateway identityUserGateway,
                                                   IdentityUserActionGateway identityUserActionGateway,
                                                   NotificationCommandPublisher notificationCommandPublisher,
                                                   RequestActorResolver actorResolver) {
        return new UserNotificationService(
                identityUserGateway,
                identityUserActionGateway,
                notificationCommandPublisher,
                actorResolver
        );
    }

    private static IdentityUser user(String id, String username, String email, String firstName) {
        return new IdentityUser(
                id,
                username,
                email,
                firstName,
                "Teste",
                true,
                false,
                1L,
                Map.of()
        );
    }
}
