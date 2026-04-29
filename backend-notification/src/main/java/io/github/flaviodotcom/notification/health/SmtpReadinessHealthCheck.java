package io.github.flaviodotcom.notification.health;

import io.github.flaviodotcom.notification.config.properties.MailerProperties;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.IOException;
import java.time.Duration;

@Readiness
@AllArgsConstructor
@ApplicationScoped
public class SmtpReadinessHealthCheck implements HealthCheck {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final MailerProperties mailerProperties;

    @Override
    public HealthCheckResponse call() {
        if (this.mailerProperties.mock()) {
            return HealthCheckResponse.named("backend-notification-smtp")
                    .up()
                    .withData("mock", true)
                    .build();
        }

        try (var socket = new Socket()) {
            socket.connect(
                    new InetSocketAddress(this.mailerProperties.host(), this.mailerProperties.port()),
                    Math.toIntExact(TIMEOUT.toMillis())
            );
        } catch (IOException exception) {
            throw new IllegalStateException("SMTP readiness check failed.", exception);
        }
        return HealthCheckResponse.up("backend-notification-smtp");
    }
}
