package io.github.flaviodotcom.resources;

import io.github.flaviodotcom.dto.CreateGroupRequest;
import io.github.flaviodotcom.dto.UpdateGroupRequest;
import io.github.flaviodotcom.resources.support.QueryCriteriaMapper;
import io.github.flaviodotcom.service.GroupService;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/v1/groups")
@AllArgsConstructor
public class GroupResource {

    private final GroupService groupService;
    private final QueryCriteriaMapper queryCriteriaMapper;

    @GET
    @Operation(summary = "Find groups by filters")
    public Response findGroups(@Context UriInfo uriInfo) {
        return Response.ok(this.groupService.findGroups(this.queryCriteriaMapper.toGroupCriteria(uriInfo))).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Find a group by id")
    public Response findGroupById(@PathParam("id") String id) {
        return Response.ok(this.groupService.findGroupById(id)).build();
    }

    @GET
    @Path("{id}/members")
    @Operation(summary = "Find members of a group")
    public Response findGroupMembers(@PathParam("id") String id) {
        return Response.ok(this.groupService.findGroupMembers(id)).build();
    }

    @POST
    @Operation(summary = "Create a group")
    public Response createGroup(@Valid CreateGroupRequest request, @Context UriInfo uriInfo) {
        var createdGroup = this.groupService.createGroup(request);
        var location = uriInfo.getAbsolutePathBuilder().path(createdGroup.id()).build();
        return Response.created(location).entity(createdGroup).build();
    }

    @PUT
    @Path("{id}")
    @Operation(summary = "Update a group")
    public Response updateGroup(@PathParam("id") String id, @Valid UpdateGroupRequest request) {
        return Response.ok(this.groupService.updateGroup(id, request)).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(summary = "Delete a group")
    public Response deleteGroup(@PathParam("id") String id) {
        this.groupService.deleteGroup(id);
        return Response.noContent().build();
    }
}
