package io.github.flaviodotcom.resources;

import io.github.flaviodotcom.dto.CreateGroupRequest;
import io.github.flaviodotcom.resources.support.QueryCriteriaMapper;
import io.github.flaviodotcom.service.GroupService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
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

    @POST
    @Operation(summary = "Create a group")
    public Response createGroup(@Valid CreateGroupRequest request, @Context UriInfo uriInfo) {
        var createdGroup = this.groupService.createGroup(request);
        var location = uriInfo.getAbsolutePathBuilder().path(createdGroup.id()).build();
        return Response.created(location).entity(createdGroup).build();
    }
}
