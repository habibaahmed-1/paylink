package com.paylink.paylink;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthControllerTest {
    @Test
    void healthEndpointReturnsOk() {
        HealthController controller = new HealthController();
        assertEquals("ok", controller.health().get("status"));
    }
}