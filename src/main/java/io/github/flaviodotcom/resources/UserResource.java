package io.github.flaviodotcom.resources;

import io.github.flaviodotcom.dto.CreateUserRequest;
import io.github.flaviodotcom.dto.PatchUserRequest;
import io.github.flaviodotcom.dto.UpdateUserRequest;
import io.github.flaviodotcom.resources.support.QueryCriteriaMapper;
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
    private final QueryCriteriaMapper queryCriteriaMapper;

    @GET
    @Operation(summary = "Find users by filters")
    public Response findUsers(@Context UriInfo uriInfo) {
        return Response.ok(this.userService.findUsers(
                this.queryCriteriaMapper.toUserCriteria(uriInfo),
                this.queryCriteriaMapper.toUserResponseOptions(uriInfo),
                this.queryCriteriaMapper.toUserPageRequest(uriInfo)
        )).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Find a user by id")
    public Response findUserById(@PathParam("id") String id, @Context UriInfo uriInfo) {
        return Response.ok(this.userService.findUserById(id, this.queryCriteriaMapper.toUserResponseOptions(uriInfo))).build();
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
}
