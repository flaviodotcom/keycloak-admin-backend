package io.github.flaviodotcom.config.restassured;

import io.github.flaviodotcom.service.events.RequestActorResolver;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class ActorHeaderFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification request,
                           FilterableResponseSpecification response,
                           FilterContext context) {

        if (!request.getHeaders()
                .hasHeaderWithName(RequestActorResolver.ACTOR_HEADER)) {

            request.header(
                    RequestActorResolver.ACTOR_HEADER,
                    "integration-test@example.com"
            );
        }

        return context.next(request, response);
    }
}