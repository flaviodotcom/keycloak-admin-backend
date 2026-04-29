package io.github.flaviodotcom.service.notifications;

import java.util.List;
import java.util.Map;

public record EmailNotificationCommand(
        String commandId,
        int schemaVersion,
        String requestedBy,
        String from,
        List<String> to,
        List<String> cc,
        List<String> bcc,
        String subject,
        String textBody,
        String htmlBody,
        List<Object> attachments,
        Map<String, String> metadata
) {
}
