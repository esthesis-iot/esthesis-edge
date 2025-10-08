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
    void testYyyyMMdd_HHmmssToInstant() {
        String date = "2023-10-05 12:34:56";
        Instant result = EnedisUtil.yyyyMMdd_HHmmssToInstant(date);
        assertEquals(Instant.parse("2023-10-05T12:34:56Z"), result);
    }

    @Test
    void testYyyyMMddTHHmmssSSSZToInstantToInstant() {
        String date = "2023-10-05T12:34:56.789Z";
        Instant result = EnedisUtil.yyyyMMddTHHmmssSSSZToInstantToInstant(date);
        assertEquals(Instant.parse("2023-10-05T12:34:56.789Z"), result);
    }

}