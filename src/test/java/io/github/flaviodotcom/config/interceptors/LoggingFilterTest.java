package io.github.flaviodotcom.config.interceptors;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoggingFilterTest {

    @Test
    void givenCorrelationIdHeader_WhenRequestEnds_ThenReuseCorrelationIdInResponseHeader() {
        var filter = new LoggingFilter();
        var requestContext = this.requestContext("GET", "v1/users", "client-correlation-id");
        var responseContext = this.responseContext(200);

        filter.filter(requestContext);
        filter.filter(requestContext, responseContext);

        assertEquals("client-correlation-id", responseContext.getHeaders().getFirst(LoggingFilter.CORRELATION_ID_HEADER));
    }

    @Test
    void givenMissingCorrelationIdHeader_WhenRequestEnds_ThenGenerateCorrelationIdInResponseHeader() {
        var filter = new LoggingFilter();
        var requestContext = this.requestContext("POST", "v1/users", null);
        var responseContext = this.responseContext(201);

        filter.filter(requestContext);
        filter.filter(requestContext, responseContext);

        var correlationId = responseContext.getHeaders().getFirst(LoggingFilter.CORRELATION_ID_HEADER);
        assertNotNull(correlationId);
        UUID.fromString(correlationId.toString());
    }

    private ContainerRequestContext requestContext(String method, String path, String correlationId) {
        var requestContext = mock(ContainerRequestContext.class);
        var uriInfo = mock(UriInfo.class);
        var properties = new HashMap<String, Object>();

        when(requestContext.getHeaderString(LoggingFilter.CORRELATION_ID_HEADER)).thenReturn(correlationId);
        when(requestContext.getMethod()).thenReturn(method);
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn(path);
        doAnswer(invocation -> {
            properties.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(requestContext).setProperty(anyString(), any());
        when(requestContext.getProperty(anyString())).thenAnswer(invocation -> properties.get(invocation.getArgument(0)));

        return requestContext;
    }

    private ContainerResponseContext responseContext(int status) {
        var responseContext = mock(ContainerResponseContext.class);
        when(responseContext.getStatus()).thenReturn(status);
        when(responseContext.getHeaders()).thenReturn(new MultivaluedHashMap<>());
        return responseContext;
    }
}
