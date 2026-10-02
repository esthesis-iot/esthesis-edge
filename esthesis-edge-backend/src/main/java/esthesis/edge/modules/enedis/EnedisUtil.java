package esthesis.edge.modules.enedis;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * Utility class for Enedis-related data manipulation.
 */
public class EnedisUtil {

  private static final DateTimeFormatter MESURE_DATE_TIME =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private EnedisUtil() {
  }

  /**
   * Convert an Instant value to a YYYY-MM-DD string.
   *
   * @param instant The Instant value to convert.
   * @return The given Instant value as a YYYY-MM-DD string.
   */
  public static String instantToYmd(Instant instant) {
    return DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(Date.from(instant));
  }

  /**
   * Converts a YYYY-MM-DD string to an Instant (ISO-8601 date).
   *
   * @param date The date to convert.
   * @return The given date as an ISO-8601 date.
   */
  public static Instant ymdToInstant(String date) {
    return Instant.parse(date + "T23:59:59Z");
  }

  /**
   * Converts a point date as returned by the Enedis mesure_synchrone_auto v2 API to an Instant. A
   * date-only value (YYYY-MM-DD) is converted like {@link #ymdToInstant(String)}, whereas a value
   * with a time ("YYYY-MM-DD HH:mm:ss") is read as UTC.
   *
   * @param d The date string to convert.
   * @return The given date as an Instant.
   * @throws java.time.format.DateTimeParseException If the date is in neither format.
   */
  public static Instant mesureDateToInstant(String d) {
    if (d.length() == 10) {
      return ymdToInstant(d);
    }
    return LocalDateTime.parse(d, MESURE_DATE_TIME).toInstant(ZoneOffset.UTC);
  }
}
