package io.github.flaviodotcom.infrastructure.interception.contracts;

import java.util.Map;

public interface ActionPayload {
    String actionSubjectId();

    Map<String, Object> actionMetadata();
}
