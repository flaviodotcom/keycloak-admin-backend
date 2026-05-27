package io.github.flaviodotcom.audit.resources;

import io.github.flaviodotcom.audit.resources.query.AuditEventQueryCriteriaMapper;
import io.github.flaviodotcom.audit.service.AuditEventService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Path("/v1/audit-events")
public class AuditEventResource {

    private final AuditEventQueryCriteriaMapper queryCriteriaMapper;
    private final AuditEventService service;

    @GET
    public Response findEvents(@Context UriInfo uriInfo) {
        return Response.ok(this.service.findEvents(
                queryCriteriaMapper.toCriteria(uriInfo),
                queryCriteriaMapper.toPageRequest(uriInfo)
        )).build();
    }

    @GET
    @Path("/{eventId}")
    public Response findByEventId(@PathParam("eventId") String eventId) {
        return Response.ok(this.service.findEventById(eventId)).build();
    }
}
