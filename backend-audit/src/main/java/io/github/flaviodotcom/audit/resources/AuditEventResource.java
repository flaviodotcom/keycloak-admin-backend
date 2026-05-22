package io.github.flaviodotcom.audit.resources;

import io.github.flaviodotcom.audit.dto.pagination.PageRequest;
import io.github.flaviodotcom.audit.service.AuditEventService;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Path("/v1/audit-events")
public class AuditEventResource {

    private final AuditEventService service;

    @GET
    public Response findEvents(@BeanParam PageRequest pageRequest) {
        return Response.ok(this.service.findEvents(pageRequest)).build();
    }

    @GET
    @Path("/{eventId}")
    public Response findByEventId(@PathParam("eventId") String eventId) {
        return Response.ok(service.findEventById(eventId)).build();
    }
}
