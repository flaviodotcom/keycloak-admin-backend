package io.github.flaviodotcom.config.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.Map;

public class AttributeMapValidator implements ConstraintValidator<ValidAttributes, Map<String, List<String>>> {

    private static final String ATTRIBUTE_NAME_REQUIRED = "{validation.attribute-name.required}";
    private static final String ATTRIBUTE_VALUES_REQUIRED = "{validation.attribute-values.required}";
    private static final String ATTRIBUTE_VALUES_EMPTY = "{validation.attribute-values.empty}";
    private static final String ATTRIBUTE_VALUE_REQUIRED = "{validation.attribute-value.required}";

    @Override
    public boolean isValid(Map<String, List<String>> attributes, ConstraintValidatorContext context) {
        if (attributes == null || attributes.isEmpty()) {
            return true;
        }

        for (var attribute : attributes.entrySet()) {
            if (attribute.getKey() == null || attribute.getKey().isBlank()) {
                return this.reject(context, ATTRIBUTE_NAME_REQUIRED);
            }

            var values = attribute.getValue();
            if (values == null) {
                return this.reject(context, ATTRIBUTE_VALUES_REQUIRED);
            }

            if (values.isEmpty()) {
                return this.reject(context, ATTRIBUTE_VALUES_EMPTY);
            }

            for (var value : values) {
                if (value == null || value.isBlank()) {
                    return this.reject(context, ATTRIBUTE_VALUE_REQUIRED);
                }
            }
        }

        return true;
    }

    private boolean reject(ConstraintValidatorContext context, String messageTemplate) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
        return false;
    }
}
