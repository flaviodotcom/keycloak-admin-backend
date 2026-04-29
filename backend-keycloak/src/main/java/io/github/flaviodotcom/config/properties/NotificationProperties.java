package io.github.flaviodotcom.config.properties;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "notification")
public interface NotificationProperties {

    Commands commands();

    @WithName("update-password")
    UpdatePassword updatePassword();

    interface Commands {

        boolean enabled();
    }

    interface UpdatePassword {

        String subject();

        @WithName("text-body")
        String textBody();
    }
}
