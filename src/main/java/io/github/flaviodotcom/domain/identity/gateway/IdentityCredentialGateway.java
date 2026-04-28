package io.github.flaviodotcom.domain.identity.gateway;

import java.util.List;

public interface IdentityCredentialGateway {

    void resetPassword(String userId, String value, boolean temporary);

    void updateRequiredActions(String userId, List<String> actions);
}
