package esthesis.edge.modules.fronius;

import java.time.Instant;
import java.time.OffsetDateTime;

/**
 * Utility class for Fronius-related data manipulation.
 */
public class FroniusUtil {

    private FroniusUtil() {
    }

    /**
     * Converts an ISO 8601 date-time string with offset (e.g., "2025-03-13T13:01:41+02:00") to an Instant.
     *
     * @param date The offset date-time string to convert.
     * @return The corresponding Instant in UTC.
     */
    public static Instant offsetDateTimeToInstant(String date) {
        return OffsetDateTime.parse(date).toInstant();
    }
}
