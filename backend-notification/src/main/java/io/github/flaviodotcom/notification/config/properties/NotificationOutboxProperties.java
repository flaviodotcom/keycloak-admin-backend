package io.github.flaviodotcom.notification.config.properties;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.time.Duration;

@ConfigMapping(prefix = "notification.outbox")
public interface NotificationOutboxProperties {

    @WithDefault("10")
    int batchSize();

    @WithDefault("2s")
    String processingInterval();

    @WithDefault("3")
    int maxAttempts();

    @WithDefault("30s")
    Duration retryDelay();
}
