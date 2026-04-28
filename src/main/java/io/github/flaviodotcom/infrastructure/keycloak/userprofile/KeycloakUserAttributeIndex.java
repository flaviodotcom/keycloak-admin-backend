package io.github.flaviodotcom.infrastructure.keycloak.userprofile;

import io.github.flaviodotcom.domain.identity.gateway.IdentityUserAttributeGateway;
import io.github.flaviodotcom.domain.shared.SearchableAttributeName;
import io.github.flaviodotcom.domain.shared.TextFilterMatcher;
import io.github.flaviodotcom.i18n.Messages;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakUserAttributeIndex {

    private final IdentityUserAttributeGateway identityUserAttributeGateway;

    public KeycloakUserAttributeDefinitions definitionsFor(Set<String> attributeNames) {
        if (attributeNames.isEmpty()) {
            return new KeycloakUserAttributeDefinitions(Map.of());
        }
        return new KeycloakUserAttributeDefinitions(this.identityUserAttributeGateway.findAttributes(attributeNames));
    }

    public Map<String, List<String>> index(Map<String, List<String>> attributes) {
        return this.index(attributes, this.definitionsFor(attributes.keySet()));
    }

    public Map<String, List<String>> index(Map<String, List<String>> attributes,
                                           KeycloakUserAttributeDefinitions definitions) {
        var indexedAttributes = new LinkedHashMap<String, List<String>>();
        for (var attribute : attributes.entrySet()) {
            var name = attribute.getKey();
            SearchableAttributeName.requirePublicName(name);
            var values = List.copyOf(Objects.requireNonNull(attribute.getValue(), Messages.getDefault("error.attribute-values.required")));
            indexedAttributes.put(name, values);

            if (definitions.isInsensitive(name)) {
                indexedAttributes.put(SearchableAttributeName.toInternalName(name), this.normalize(values));
            }
        }
        return Map.copyOf(indexedAttributes);
    }

    public Map<String, String> toSearchAttributes(Map<String, String> attributes,
                                                  KeycloakUserAttributeDefinitions definitions) {
        var searchAttributes = new LinkedHashMap<String, String>();
        for (var attribute : attributes.entrySet()) {
            SearchableAttributeName.requirePublicName(attribute.getKey());
            var searchName = definitions.isInsensitive(attribute.getKey())
                    ? SearchableAttributeName.toInternalName(attribute.getKey())
                    : attribute.getKey();
            var searchValue = definitions.isInsensitive(attribute.getKey())
                    ? TextFilterMatcher.normalize(attribute.getValue())
                    : attribute.getValue();
            searchAttributes.put(searchName, searchValue);
        }
        return Map.copyOf(searchAttributes);
    }

    public boolean matches(Map<String, String> requestedAttributes,
                           Map<String, List<String>> currentAttributes,
                           boolean exact,
                           KeycloakUserAttributeDefinitions definitions) {
        for (var requestedAttribute : requestedAttributes.entrySet()) {
            SearchableAttributeName.requirePublicName(requestedAttribute.getKey());
            var values = currentAttributes.get(requestedAttribute.getKey());
            if (values == null || values.isEmpty()) {
                return false;
            }

            var matched = values.stream().anyMatch(value -> definitions.isInsensitive(requestedAttribute.getKey())
                    ? TextFilterMatcher.matches(requestedAttribute.getValue(), value, exact)
                    : TextFilterMatcher.matchesSensitive(requestedAttribute.getValue(), value, exact));
            if (!matched) {
                return false;
            }
        }

        return true;
    }

    private List<String> normalize(List<String> values) {
        return values.stream()
                .map(TextFilterMatcher::normalize)
                .toList();
    }
}
