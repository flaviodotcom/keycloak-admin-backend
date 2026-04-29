package io.github.flaviodotcom.config.properties;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "identity.events")
public interface IdentityEventProperties {

    boolean enabled();
}
