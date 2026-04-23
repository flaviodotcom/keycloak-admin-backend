package io.github.flaviodotcom.resources;

import io.github.flaviodotcom.dto.CreateRoleRequest;
import io.github.flaviodotcom.resources.support.QueryCriteriaMapper;
import io.github.flaviodotcom.service.RoleService;
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

@Path("/v1/roles")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@AllArgsConstructor
public class RoleResource {

    private final RoleService roleService;
    private final QueryCriteriaMapper queryCriteriaMapper;

    @GET
    @Operation(summary = "Find roles by filters")
    public Response findRoles(@Context UriInfo uriInfo) {
        return Response.ok(this.roleService.findRoles(this.queryCriteriaMapper.toRoleCriteria(uriInfo))).build();
    }

    @POST
    @Operation(summary = "Create a role")
    public Response createRole(@Valid CreateRoleRequest request, @Context UriInfo uriInfo) {
        var createdRole = this.roleService.createRole(request);
        var location = uriInfo.getAbsolutePathBuilder().path(createdRole.id()).build();
        return Response.created(location).entity(createdRole).build();
    }
}
