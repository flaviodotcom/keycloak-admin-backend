package io.github.flaviodotcom.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record AttachmentRequest(
        @NotBlank String fileName,
        @NotBlank String contentType,
        @NotBlank String contentBase64
) {
}
