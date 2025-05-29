package esthesis.edge.modules.fronius;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class FroniusUtilTest {

    @Test
    void offsetDateTimeToInstant() {
        String date1 = "2025-08-04T12:00:00+00:00";
        String date2 = "2025-08-04T14:00:00+02:00";
        String date3 = "2025-08-04T10:00:00-02:00";

        assertEquals("2025-08-04T12:00:00Z", FroniusUtil.offsetDateTimeToInstant(date1).toString());
        assertEquals("2025-08-04T12:00:00Z", FroniusUtil.offsetDateTimeToInstant(date2).toString());
        assertEquals("2025-08-04T12:00:00Z", FroniusUtil.offsetDateTimeToInstant(date3).toString());
    }
}