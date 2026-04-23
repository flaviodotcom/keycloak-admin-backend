package io.github.flaviodotcom.exceptions.providers;

import io.github.flaviodotcom.exceptions.BusinessException;
import io.github.flaviodotcom.exceptions.ProblemBuilder;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BusinessExceptionProvider implements ExceptionMapper<BusinessException> {

    @Override
    public Response toResponse(BusinessException exception) {
        return ProblemBuilder.build(422, "Business rule violation", exception.getMessage());
    }
}
