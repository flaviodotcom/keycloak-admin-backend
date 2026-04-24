package io.github.flaviodotcom.resources;

import io.github.flaviodotcom.dto.CreateUserAttributeRequest;
import io.github.flaviodotcom.service.UserAttributeService;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/v1/users/attributes")
@AllArgsConstructor
public class UserAttributeResource {

    private final UserAttributeService userAttributeService;

    @POST
    @Operation(summary = "Create a user attribute")
    public Response createAttribute(@Valid CreateUserAttributeRequest request, @Context UriInfo uriInfo) {
        var createdAttribute = this.userAttributeService.createAttribute(request);
        var location = uriInfo.getAbsolutePathBuilder().path(createdAttribute.name()).build();
        return Response.created(location).entity(createdAttribute).build();
    }
}
