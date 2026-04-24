package io.github.flaviodotcom.dto;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityUserAttributeCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CreateUserAttributeRequest(
        @NotBlank(message = "name is required")
        String name,
        @NotEmpty(message = "displayName is required")
        Map<@NotBlank(message = "displayName locale is required") String, @NotBlank(message = "displayName text is required") String> displayName,
        @NotNull(message = "insensitive is required")
        Boolean insensitive,
        Boolean required,
        Boolean multivalued
) {

    public CreateUserAttributeRequest {
        required = required == null ? Boolean.FALSE : required;
        multivalued = multivalued == null ? Boolean.FALSE : multivalued;
    }

    public CreateIdentityUserAttributeCommand toCommand() {
        return new CreateIdentityUserAttributeCommand(
                this.name,
                this.displayName,
                this.insensitive,
                this.required,
                this.multivalued
        );
    }
}
