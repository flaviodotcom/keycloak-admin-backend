package io.github.flaviodotcom.resources;

import io.github.flaviodotcom.dto.user.CreateUserRequest;
import io.github.flaviodotcom.dto.user.PatchUserRequest;
import io.github.flaviodotcom.dto.user.RequiredActionsRequest;
import io.github.flaviodotcom.dto.user.ResetPasswordRequest;
import io.github.flaviodotcom.dto.user.UpdateUserRequest;
import io.github.flaviodotcom.resources.query.UserQueryCriteriaMapper;
import io.github.flaviodotcom.service.UserService;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/v1/users")
@AllArgsConstructor
public class UserResource {

    private final UserService userService;
    private final UserQueryCriteriaMapper queryCriteriaMapper;

    @GET
    @Operation(summary = "Find users by filters")
    public Response findUsers(@Context UriInfo uriInfo) {
        return Response.ok(this.userService.findUsers(
                this.queryCriteriaMapper.toCriteria(uriInfo),
                this.queryCriteriaMapper.toResponseOptions(uriInfo),
                this.queryCriteriaMapper.toPageRequest(uriInfo)
        )).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Find a user by id")
    public Response findUserById(@PathParam("id") String id, @Context UriInfo uriInfo) {
        return Response.ok(this.userService.findUserById(id, this.queryCriteriaMapper.toResponseOptions(uriInfo))).build();
    }

    @POST
    @Operation(summary = "Create a user")
    public Response createUser(@Valid CreateUserRequest request, @Context UriInfo uriInfo) {
        var createdUser = this.userService.createUser(request);
        var location = uriInfo.getAbsolutePathBuilder().path(createdUser.id()).build();
        return Response.created(location).entity(createdUser).build();
    }

    @PUT
    @Path("{id}")
    @Operation(summary = "Update a user")
    public Response updateUser(@PathParam("id") String id, @Valid UpdateUserRequest request) {
        return Response.ok(this.userService.updateUser(id, request)).build();
    }

    @PATCH
    @Path("{id}")
    @Operation(summary = "Partially update a user")
    public Response patchUser(@PathParam("id") String id, @Valid PatchUserRequest request) {
        return Response.ok(this.userService.patchUser(id, request)).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(summary = "Delete a user")
    public Response deleteUser(@PathParam("id") String id) {
        this.userService.deleteUser(id);
        return Response.noContent().build();
    }

    @POST
    @Path("{id}/actions/update-password-email")
    @Operation(summary = "Send update password email")
    public Response sendUpdatePasswordEmail(@PathParam("id") String id) {
        this.userService.sendUpdatePasswordEmail(id);
        return Response.noContent().build();
    }

    @POST
    @Path("{id}/groups/{groupId}")
    @Operation(summary = "Assign a group to a user")
    public Response assignGroup(@PathParam("id") String id, @PathParam("groupId") String groupId) {
        this.userService.assignGroup(id, groupId);
        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}/groups/{groupId}")
    @Operation(summary = "Unassign a group from a user")
    public Response unassignGroup(@PathParam("id") String id, @PathParam("groupId") String groupId) {
        this.userService.unassignGroup(id, groupId);
        return Response.noContent().build();
    }

    @POST
    @Path("{id}/roles/realm/{roleName}")
    @Operation(summary = "Assign a realm role to a user")
    public Response assignRealmRole(@PathParam("id") String id, @PathParam("roleName") String roleName) {
        this.userService.assignRealmRole(id, roleName);
        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}/roles/realm/{roleName}")
    @Operation(summary = "Unassign a realm role from a user")
    public Response unassignRealmRole(@PathParam("id") String id, @PathParam("roleName") String roleName) {
        this.userService.unassignRealmRole(id, roleName);
        return Response.noContent().build();
    }

    @POST
    @Path("{id}/roles/clients/{clientId}/{roleName}")
    @Operation(summary = "Assign a client role to a user")
    public Response assignClientRole(@PathParam("id") String id,
                                     @PathParam("clientId") String clientId,
                                     @PathParam("roleName") String roleName) {
        this.userService.assignClientRole(id, clientId, roleName);
        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}/roles/clients/{clientId}/{roleName}")
    @Operation(summary = "Unassign a client role from a user")
    public Response unassignClientRole(@PathParam("id") String id,
                                       @PathParam("clientId") String clientId,
                                       @PathParam("roleName") String roleName) {
        this.userService.unassignClientRole(id, clientId, roleName);
        return Response.noContent().build();
    }

    @PUT
    @Path("{id}/password")
    @Operation(summary = "Reset a user password")
    public Response resetPassword(@PathParam("id") String id, @Valid ResetPasswordRequest request) {
        this.userService.resetPassword(id, request);
        return Response.noContent().build();
    }

    @PUT
    @Path("{id}/required-actions")
    @Operation(summary = "Update user required actions")
    public Response updateRequiredActions(@PathParam("id") String id, @Valid RequiredActionsRequest request) {
        this.userService.updateRequiredActions(id, request);
        return Response.noContent().build();
    }

    @GET
    @Path("{id}/sessions")
    @Operation(summary = "Find user sessions")
    public Response findSessions(@PathParam("id") String id) {
        return Response.ok(this.userService.findSessions(id)).build();
    }

    @DELETE
    @Path("{id}/sessions")
    @Operation(summary = "Logout a user from all sessions")
    public Response logout(@PathParam("id") String id) {
        this.userService.logout(id);
        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}/sessions/{sessionId}")
    @Operation(summary = "Delete a user session")
    public Response deleteSession(@PathParam("id") String id, @PathParam("sessionId") String sessionId) {
        this.userService.deleteSession(id, sessionId);
        return Response.noContent().build();
    }
}
