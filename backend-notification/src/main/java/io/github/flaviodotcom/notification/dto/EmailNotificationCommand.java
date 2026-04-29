package io.github.flaviodotcom.notification.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record EmailNotificationCommand(
        @NotBlank String commandId,
        @NotNull Integer schemaVersion,
        @NotBlank String requestedBy,
        String from,
        @NotEmpty List<@Email String> to,
        List<@Email String> cc,
        List<@Email String> bcc,
        @NotBlank String subject,
        String textBody,
        String htmlBody,
        List<@Valid AttachmentRequest> attachments,
        Map<String, String> metadata
) {
}
