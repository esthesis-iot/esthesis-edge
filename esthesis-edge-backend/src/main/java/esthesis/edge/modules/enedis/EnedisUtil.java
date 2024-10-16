package esthesis.edge.modules.enedis;

import java.time.Instant;
import java.util.Date;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * Utility class for Enedis-related data manipulation.
 */
public class EnedisUtil {

  private EnedisUtil() {
  }

  /**
   * Convert an Instant value to a YYYY-MM-DD string.
   *
   * @param instant The Instant value to convert.
   * @return The given Instant value as a YYYY-MM-DD string.
   */
  public static String InstantToYmd(Instant instant) {
    return DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(Date.from(instant));
  }

  /**
   * Converts a YYYY-MM-DD string to an Instant (ISO-8601 date).
   *
   * @param date The date to convert.
   * @return The given date as an ISO-8601 date.
   */
  public static Instant YmdToInstant(String date) {
    return Instant.parse(date + "T23:59:59Z");
  }
}
