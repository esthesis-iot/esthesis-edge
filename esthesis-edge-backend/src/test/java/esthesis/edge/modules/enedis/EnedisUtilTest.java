package esthesis.edge.modules.enedis;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

class EnedisUtilTest {

    @Test
    void testInstantToYmd() {        
        Instant instant = Instant.parse("2023-10-05T12:34:56Z");        
        String result = EnedisUtil.instantToYmd(instant);        
        assertEquals("2023-10-05", result);
    }

    @Test
    void testYmdToInstant() {        
        String date = "2023-10-05";
        Instant result = EnedisUtil.ymdToInstant(date);
        assertEquals(Instant.parse("2023-10-05T23:59:59Z"), result);
    }

    @Test
    void mesureDateDateOnlyIsEndOfDayUtc() {
        assertEquals(Instant.parse("2026-09-25T23:59:59Z"), EnedisUtil.mesureDateToInstant("2026-09-25"));
    }

    @Test
    void mesureDateDateTimeIsUtc() {
        assertEquals(Instant.parse("2026-09-25T08:44:22Z"), EnedisUtil.mesureDateToInstant("2026-09-25 08:44:22"));
    }

    @Test
    void mesureDateRejectsPlaceholder() {
        assertThrows(DateTimeParseException.class, () -> EnedisUtil.mesureDateToInstant("20XX-XX-XX"));
    }

}