package esthesis.edge.modules.enedis;

import org.junit.jupiter.api.Test;

import java.time.Instant;

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
    void testIsoInstantToInstant() {
        String date = "2023-10-05T12:34:56.789Z";
        Instant result = EnedisUtil.isoInstantToInstant(date);
        assertEquals(Instant.parse("2023-10-05T12:34:56.789Z"), result);
    }

    @Test
    void testItcDateToInstantPlusHHMM() {
        String date = "2022-01-02T00:00:00+0100";
        Instant result = EnedisUtil.itcDateToInstant(date);
        assertEquals(Instant.parse("2022-01-01T23:00:00Z"), result);
    }

    @Test
    void testItcDateToInstantZulu() {
        String date = "2023-10-05T12:34:56Z";
        Instant result = EnedisUtil.itcDateToInstant(date);
        assertEquals(Instant.parse("2023-10-05T12:34:56Z"), result);
    }

    @Test
    void testItcDateToInstantWithMillis() {
        String date = "2019-05-06T00:00:00.000Z";
        Instant result = EnedisUtil.itcDateToInstant(date);
        assertEquals(Instant.parse("2019-05-06T00:00:00Z"), result);
    }

}