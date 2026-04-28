package io.github.flaviodotcom.dto.user;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "{validation.password.required}")
        String value,
        Boolean temporary
) {

    public ResetPasswordRequest {
        temporary = temporary == null ? Boolean.FALSE : temporary;
    }
}
