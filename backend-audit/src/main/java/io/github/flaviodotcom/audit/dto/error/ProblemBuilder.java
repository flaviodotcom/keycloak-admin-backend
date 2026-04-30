package io.github.flaviodotcom.audit.dto.error;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public final class ProblemBuilder {

    private ProblemBuilder() {
    }

    public static Response build(int status, String title, String detail) {
        return build(new Problem(status, title, detail));
    }

    public static Response build(Problem problem) {
        return Response.status(problem.getStatus())
                .entity(problem)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
