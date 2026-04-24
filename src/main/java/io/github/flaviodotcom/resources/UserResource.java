package io.github.flaviodotcom.resources;

import io.github.flaviodotcom.dto.CreateUserRequest;
import io.github.flaviodotcom.resources.support.QueryCriteriaMapper;
import io.github.flaviodotcom.service.UserService;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
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
        return Response.ok(this.userService.findUsers(this.queryCriteriaMapper.toUserCriteria(uriInfo))).build();
    }

    @POST
    @Operation(summary = "Create a user")
    public Response createUser(@Valid CreateUserRequest request, @Context UriInfo uriInfo) {
        var createdUser = this.userService.createUser(request);
        var location = uriInfo.getAbsolutePathBuilder().path(createdUser.id()).build();
        return Response.created(location).entity(createdUser).build();
    }
}
