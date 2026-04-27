package io.github.flaviodotcom.resources;

import io.github.flaviodotcom.dto.CreateRoleRequest;
import io.github.flaviodotcom.dto.UpdateRoleRequest;
import io.github.flaviodotcom.resources.support.QueryCriteriaMapper;
import io.github.flaviodotcom.service.RoleService;
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

@Path("/v1/roles")
@AllArgsConstructor
public class RoleResource {

    private final RoleService roleService;
    private final QueryCriteriaMapper queryCriteriaMapper;

    @GET
    @Operation(summary = "Find roles by filters")
    public Response findRoles(@Context UriInfo uriInfo) {
        return Response.ok(this.roleService.findRoles(this.queryCriteriaMapper.toRoleCriteria(uriInfo))).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Find a role by id")
    public Response findRoleById(@PathParam("id") String id) {
        return Response.ok(this.roleService.findRoleById(id)).build();
    }

    @POST
    @Operation(summary = "Create a role")
    public Response createRole(@Valid CreateRoleRequest request, @Context UriInfo uriInfo) {
        var createdRole = this.roleService.createRole(request);
        var location = uriInfo.getAbsolutePathBuilder().path(createdRole.id()).build();
        return Response.created(location).entity(createdRole).build();
    }

    @PUT
    @Path("{id}")
    @Operation(summary = "Update a role")
    public Response updateRole(@PathParam("id") String id, @Valid UpdateRoleRequest request) {
        return Response.ok(this.roleService.updateRole(id, request)).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(summary = "Delete a role")
    public Response deleteRole(@PathParam("id") String id) {
        this.roleService.deleteRole(id);
        return Response.noContent().build();
    }
}
