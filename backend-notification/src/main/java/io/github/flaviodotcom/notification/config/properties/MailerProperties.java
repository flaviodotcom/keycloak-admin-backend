package io.github.flaviodotcom.notification.config.properties;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "quarkus.mailer")
public interface MailerProperties {

    String host();

    int port();

    boolean mock();
}
