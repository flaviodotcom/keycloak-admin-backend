package io.github.flaviodotcom.notification.service;

import io.github.flaviodotcom.notification.dto.AttachmentRequest;
import io.github.flaviodotcom.notification.dto.EmailNotificationCommand;
import io.quarkus.mailer.Mail;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Base64;

@ApplicationScoped
public class EmailMessageFactory {

    public Mail create(EmailNotificationCommand command) {
        if (isBlank(command.textBody()) && isBlank(command.htmlBody())) {
            throw new IllegalArgumentException("Either textBody or htmlBody must be provided.");
        }

        var mail = new Mail();
        mail.setSubject(command.subject());
        mail.setTo(command.to());

        if (!isBlank(command.from())) {
            mail.setFrom(command.from());
        }
        if (command.cc() != null && !command.cc().isEmpty()) {
            mail.setCc(command.cc());
        }
        if (command.bcc() != null && !command.bcc().isEmpty()) {
            mail.setBcc(command.bcc());
        }
        if (!isBlank(command.textBody())) {
            mail.setText(command.textBody());
        }
        if (!isBlank(command.htmlBody())) {
            mail.setHtml(command.htmlBody());
        }
        if (command.attachments() != null) {
            command.attachments().forEach(attachment -> addAttachment(mail, attachment));
        }

        return mail;
    }

    private static void addAttachment(Mail mail, AttachmentRequest attachment) {
        mail.addAttachment(
                attachment.fileName(),
                Base64.getDecoder().decode(attachment.contentBase64()),
                attachment.contentType()
        );
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
