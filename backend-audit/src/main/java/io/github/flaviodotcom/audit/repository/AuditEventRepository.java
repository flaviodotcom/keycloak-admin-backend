package io.github.flaviodotcom.audit.repository;

import io.github.flaviodotcom.audit.dto.AuditEventFilter;
import io.github.flaviodotcom.audit.entities.AuditEvent;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

@ApplicationScoped
public class AuditEventRepository implements PanacheRepository<AuditEvent> {

    public boolean existsByEventId(String eventId) {
        return count("eventId", eventId) > 0;
    }

    public Optional<AuditEvent> findByEventId(String eventId) {
        return find("eventId", eventId).firstResultOptional();
    }

    public PanacheQuery<AuditEvent> findByFilter(AuditEventFilter filter, Sort sort) {
        var clauses = new ArrayList<String>();
        var params = new HashMap<String, Object>();

        addEqualsFilter(clauses, params, "eventType", filter.eventType());
        addEqualsFilter(clauses, params, "correlationId", filter.correlationId());
        addEqualsFilter(clauses, params, "actorId", filter.actorId());
        addEqualsFilter(clauses, params, "subjectType", filter.subjectType());
        addEqualsFilter(clauses, params, "subjectId", filter.subjectId());
        addEqualsFilter(clauses, params, "topic", filter.topic());
        addRangeFilter(clauses, params, "receivedAt", filter.receivedFrom(), filter.receivedTo());

        var query = String.join(" and ", clauses);
        return find(query, sort, params);
    }

    private static void addEqualsFilter(ArrayList<String> clauses, HashMap<String, Object> params,
                                        String field, Object value) {
        if (value == null) {
            return;
        }

        clauses.add(field + " = :" + field);
        params.put(field, value);
    }

    private static void addRangeFilter(ArrayList<String> clauses, HashMap<String, Object> params,
                                       String field, Object from, Object to) {
        if (from != null) {
            clauses.add(field + " >= :" + field + "From");
            params.put(field + "From", from);
        }

        if (to != null) {
            clauses.add(field + " <= :" + field + "To");
            params.put(field + "To", to);
        }
    }
}
