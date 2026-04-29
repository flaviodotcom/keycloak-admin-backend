package io.github.flaviodotcom.service.notifications;

public interface NotificationCommandPublisher {

    void publish(EmailNotificationCommand command);
}
