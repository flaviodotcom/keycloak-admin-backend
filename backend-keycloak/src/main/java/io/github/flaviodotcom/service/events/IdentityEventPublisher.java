package io.github.flaviodotcom.service.events;

import java.util.Map;

public interface IdentityEventPublisher {

    void publish(String eventType, String subjectType, String subjectId, Map<String, Object> data);
}
