package io.github.flaviodotcom.domain.identity.gateway;

import java.util.List;

public interface IdentityUserPostCreationGateway {

    void assignGroups(String userId, List<String> groupIds);
}
