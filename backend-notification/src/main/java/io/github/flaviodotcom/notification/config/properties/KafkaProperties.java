package io.github.flaviodotcom.notification.config.properties;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "kafka")
public interface KafkaProperties {

    @WithName("bootstrap.servers")
    String bootstrapServers();
}
