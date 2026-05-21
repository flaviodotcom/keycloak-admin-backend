package io.github.flaviodotcom.infrastructure.interception.contracts;

import java.util.Map;

public record DeletedSubjectPayload(
        String id
) implements ActionPayload {

    @Override
    public String actionSubjectId() {
        return this.id;
    }

    @Override
    public Map<String, Object> actionMetadata() {
        return Map.of();
    }
}
