package io.github.flaviodotcom.infrastructure.keycloak.gateway;

import io.github.flaviodotcom.domain.identity.command.CreateIdentityUserCommand;
import io.github.flaviodotcom.domain.identity.command.PatchIdentityUserCommand;
import io.github.flaviodotcom.domain.identity.command.UpdateIdentityUserCommand;
import io.github.flaviodotcom.domain.identity.criteria.UserSearchCriteria;
import io.github.flaviodotcom.domain.identity.gateway.IdentityUserGateway;
import io.github.flaviodotcom.domain.identity.gateway.IdentityUserPostCreationGateway;
import io.github.flaviodotcom.domain.identity.model.IdentityUser;
import io.github.flaviodotcom.infrastructure.keycloak.candidate.KeycloakUserCandidateFinder;
import io.github.flaviodotcom.infrastructure.keycloak.mapper.KeycloakRepresentationMapper;
import io.github.flaviodotcom.infrastructure.keycloak.matcher.KeycloakUserMatcher;
import io.github.flaviodotcom.infrastructure.keycloak.support.CreatedResourceLocation;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakAdminSupport;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakErrorContext;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakHttpResponseHandler;
import io.github.flaviodotcom.infrastructure.keycloak.support.KeycloakUserAttributeIndex;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.AllArgsConstructor;

import java.util.List;

@ApplicationScoped
@AllArgsConstructor
public class KeycloakUserGateway implements IdentityUserGateway {

    private final KeycloakAdminSupport keycloak;
    private final KeycloakUserCandidateFinder candidateFinder;
    private final KeycloakRepresentationMapper mapper;
    private final KeycloakUserMatcher matcher;
    private final KeycloakUserAttributeIndex attributeIndex;
    private final IdentityUserPostCreationGateway postCreationGateway;

    @Override
    public List<IdentityUser> findUsers(UserSearchCriteria criteria) {
        try {
            return this.candidateFinder.findCandidates(criteria).stream()
                    .map(this.mapper::toIdentityUser)
                    .filter(user -> this.matcher.matches(user, criteria))
                    .toList();
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    @Override
    public IdentityUser findUserById(String id) {
        try {
            return this.mapper.toIdentityUser(this.keycloak.users().get(id).toRepresentation());
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    @Override
    public IdentityUser createUser(CreateIdentityUserCommand command) {
        try {
            var userRepresentation = this.mapper.toUserRepresentation(
                    command.withAttributes(this.attributeIndex.index(command.attributes()))
            );

            try (var response = this.keycloak.users().create(userRepresentation)) {
                KeycloakHttpResponseHandler.ensureCreated(response, KeycloakErrorContext.USER_CREATION);
                var userId = CreatedResourceLocation.extractId(response);
                this.assignGroupsOrRemoveUser(userId, command.groupIds());
                return this.mapper.toIdentityUser(this.keycloak.users().get(userId).toRepresentation());
            }
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception, KeycloakErrorContext.USER_CREATION);
        }
    }

    @Override
    public IdentityUser updateUser(String id, UpdateIdentityUserCommand command) {
        try {
            var userResource = this.keycloak.users().get(id);
            var userRepresentation = this.mapper.toUserRepresentation(
                    id,
                    command.withAttributes(this.attributeIndex.index(command.attributes()))
            );
            userResource.update(userRepresentation);
            return this.mapper.toIdentityUser(userResource.toRepresentation());
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    @Override
    public IdentityUser patchUser(String id, PatchIdentityUserCommand command) {
        try {
            var userResource = this.keycloak.users().get(id);
            var currentUser = userResource.toRepresentation();
            var attributes = command.attributes() == null
                    ? currentUser.getAttributes()
                    : this.attributeIndex.index(command.attributes());
            var userRepresentation = this.mapper.toUserRepresentation(id, currentUser, command, attributes);
            userResource.update(userRepresentation);
            return this.mapper.toIdentityUser(userResource.toRepresentation());
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    @Override
    public void deleteUser(String id) {
        try {
            this.keycloak.users().get(id).remove();
        } catch (WebApplicationException exception) {
            throw KeycloakHttpResponseHandler.toWebApplicationException(exception.getResponse());
        }
    }

    private void assignGroupsOrRemoveUser(String userId, List<String> groupIds) {
        try {
            this.postCreationGateway.assignGroups(userId, groupIds);
        } catch (RuntimeException exception) {
            this.removeCreatedUser(userId, exception);
            throw exception;
        }
    }

    private void removeCreatedUser(String userId, RuntimeException creationFailure) {
        try {
            this.keycloak.users().get(userId).remove();
        } catch (RuntimeException compensationFailure) {
            creationFailure.addSuppressed(compensationFailure);
        }
    }
}
