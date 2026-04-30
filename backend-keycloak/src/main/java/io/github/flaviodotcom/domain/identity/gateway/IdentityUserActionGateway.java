package io.github.flaviodotcom.domain.identity.gateway;

public interface IdentityUserActionGateway {

    void sendUpdatePasswordEmail(String userId);
}
