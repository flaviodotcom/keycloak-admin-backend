package io.github.flaviodotcom.health;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationLivenessHealthCheckTest {

    @Test
    void whenCall_ThenReturnUp() {
        var response = new ApplicationLivenessHealthCheck().call();

        assertEquals("application", response.getName());
        assertEquals(HealthCheckResponse.Status.UP, response.getStatus());
    }
}
