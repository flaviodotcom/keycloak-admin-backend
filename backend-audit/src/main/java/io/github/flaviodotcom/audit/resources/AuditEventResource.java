package io.github.flaviodotcom.audit.resources;

import io.github.flaviodotcom.audit.dto.AuditEventResponse;
import io.github.flaviodotcom.audit.repository.AuditEventRepository;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import java.util.List;

@Path("/v1/audit-events")
public class AuditEventResource {

    private static final int DEFAULT_LIMIT = 50;

    private final AuditEventRepository repository;

    public AuditEventResource(AuditEventRepository repository) {
        this.repository = repository;
    }

    @GET
    public List<AuditEventResponse> findEvents(@QueryParam("limit") Integer limit) {
        var resultLimit = limit == null ? DEFAULT_LIMIT : limit;
        return this.repository.find("order by receivedAt desc")
                .page(0, resultLimit)
                .list()
                .stream()
                .map(AuditEventResponse::fromEntity)
                .toList();
    }

    @GET
    @Path("{eventId}")
    public AuditEventResponse findByEventId(@PathParam("eventId") String eventId) {
        return this.repository.findByEventId(eventId)
                .map(AuditEventResponse::fromEntity)
                .orElseThrow(NotFoundException::new);
    }
}
