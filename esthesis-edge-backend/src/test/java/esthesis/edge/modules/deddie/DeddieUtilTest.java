package esthesis.edge.modules.deddie;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DeddieUtilTest {

    @Test
    void testToInstantWithTime() {
        Instant result = DeddieUtil.toInstant("13/08/2025 10:24");
        assertEquals(Instant.parse("2025-08-13T10:24:00Z"), result);
    }

    @Test
    void testToInstantDateOnly() {
        Instant result = DeddieUtil.toInstant("13/08/2025");
        assertEquals(Instant.parse("2025-08-13T23:59:59Z"), result);
    }


}