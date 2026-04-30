package io.github.flaviodotcom.notification.service;

import io.github.flaviodotcom.notification.dto.EmailNotificationCommand;

public interface EmailNotificationService {

    void send(EmailNotificationCommand command);
}
