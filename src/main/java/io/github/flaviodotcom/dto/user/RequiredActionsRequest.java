package io.github.flaviodotcom.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RequiredActionsRequest(
        @NotEmpty(message = "{validation.required-actions.required}")
        List<@NotBlank(message = "{validation.required-action.required}") String> actions
) {

    public RequiredActionsRequest {
        actions = actions == null ? List.of() : List.copyOf(actions);
    }
}
