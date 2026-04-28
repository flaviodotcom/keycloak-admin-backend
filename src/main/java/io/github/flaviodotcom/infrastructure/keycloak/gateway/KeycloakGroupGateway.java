package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityGroupCommand;
import io.github.flaviodotcom.domain.identity.command.UpdateIdentityGroupCommand;
import io.github.flaviodotcom.domain.identity.criteria.GroupSearchCriteria;
import io.github.flaviodotcom.domain.identity.gateway.IdentityGroupGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityGroup;
import io.github.flaviodotcom.infrastructure.keycloak.candidate.KeycloakGroupCandidateFinder;
import io.github.flaviodotcom.infrastructure.keycloak.mapper.KeycloakRepresentationMapper;
import io.github.flaviodotcom.infrastructure.keycloak.matcher.KeycloakGroupMatcher;
import io.github.flaviodotcom.infrastructure.keycloak.support.CreatedResourceLocation;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakHttpResponseHandler;
import io.github.flaviodotcom.infrastructure.keycloak.resilience.KeycloakResilienceExecutor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.AllArgsConstructor;

import java.util.List;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakGroupGateway implements IdentityGroupGateway {

    private final KeycloakAdminSupport keycloak;
    private final KeycloakGroupCandidateFinder candidateFinder;
    private final KeycloakRepresentationMapper mapper;
    private final KeycloakGroupMatcher matcher;
    private final KeycloakResilienceExecutor resilience;

    @Override
    public List<IdentityGroup> findGroups(GroupSearchCriteria criteria) {
        return this.resilience.executeRead(() -> {
            try {
                return this.candidateFinder.findCandidates().stream()
                        .map(this.mapper::toIdentityGroup)
                        .filter(group -> this.matcher.matches(group, criteria))
                        .toList();
            } catch (WebApplicationException exception) {
                throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
            }
        });
    }

    @Override
    public IdentityGroup findGroupById(String id) {
        return this.resilience.executeRead(() -> {
            try {
                return this.mapper.toIdentityGroup(this.keycloak.groups().group(id).toRepresentation());
            } catch (WebApplicationException exception) {
                throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
            }
        });
    }

    @Override
    public IdentityGroup createGroup(CreateIdentityGroupCommand command) {
        return this.resilience.executeWrite(() -> {
            try {
                var groupRepresentation = this.mapper.toGroupRepresentation(command);

                try (var response = command.parentGroupId() == null
                        ? this.keycloak.groups().add(groupRepresentation)
                        : this.keycloak.groups().group(command.parentGroupId()).subGroup(groupRepresentation)) {
                    KeycloakHttpResponseHandler.ensureCreated(response);
                    var groupId = CreatedResourceLocation.extractId(response);
                    return this.mapper.toIdentityGroup(this.keycloak.groups().group(groupId).toRepresentation());
                }
            } catch (WebApplicationException exception) {
                throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
            }
        });
    }

    @Override
    public IdentityGroup updateGroup(String id, UpdateIdentityGroupCommand command) {
        return this.resilience.executeWrite(() -> {
            try {
                var groupResource = this.keycloak.groups().group(id);
                groupResource.update(this.mapper.toGroupRepresentation(id, command));
                return this.mapper.toIdentityGroup(groupResource.toRepresentation());
            } catch (WebApplicationException exception) {
                throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
            }
        });
    }

    @Override
    public void deleteGroup(String id) {
        this.resilience.executeWrite(() -> {
            try {
                this.keycloak.groups().group(id).remove();
            } catch (WebApplicationException exception) {
                throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
            }
        });
    }
}
