package io.github.flaviodotcom.dto.userattribute;

import io.github.flaviodotcom.domain.identity.command.UpdateIdentityUserAttributeCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record UpdateUserAttributeRequest(
        @NotEmpty(message = "{validation.display-name.required}")
        Map<@NotBlank(message = "{validation.display-name.locale.required}") String, @NotBlank(message = "{validation.display-name.text.required}") String> displayName,
        @NotNull(message = "{validation.insensitive.required}")
        Boolean insensitive,
        Boolean required,
        Boolean multivalued
) {

    public UpdateUserAttributeRequest {
        required = required == null ? Boolean.FALSE : required;
        multivalued = multivalued == null ? Boolean.FALSE : multivalued;
    }

    public UpdateIdentityUserAttributeCommand toCommand(String name) {
        return new UpdateIdentityUserAttributeCommand(
                name,
                this.displayName,
                this.insensitive,
                this.required,
                this.multivalued
        );
    }
}
