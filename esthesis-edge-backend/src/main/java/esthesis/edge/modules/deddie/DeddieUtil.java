package esthesis.edge.modules.deddie;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for Deddie-related data manipulation.
 */
public class DeddieUtil {
    /**
     * Parses a date string that can be in "dd/MM/yyyy" or "dd/MM/yyyy HH:mm" format.
     * If only the date is provided, time defaults to 23:59:59 UTC.
     */
    public static Instant toInstant(String date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        try {
            // Try parsing as "dd/MM/yyyy HH:mm".
            LocalDateTime localDateTime = LocalDateTime.parse(date, dateTimeFormatter);
            return localDateTime.toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            // Fallback: treat as "dd/MM/yyyy" and append 23:59:59.
            LocalDateTime localDateTime = LocalDateTime.parse(date + " 23:59:59", dateFormatter);
            return localDateTime.toInstant(ZoneOffset.UTC);
        }
    }
}
