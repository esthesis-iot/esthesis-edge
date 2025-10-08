package esthesis.edge.modules.enedis;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
   * Converts a YYYY-MM-DD HH:mm:ss string to an Instant.
   *
   * @param date The date to convert.
   * @return The given date as an Instant.
   */
  @SuppressWarnings("java:S100")
  public static Instant yyyyMMdd_HHmmssToInstant(String date) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);

    return localDateTime.toInstant(ZoneOffset.UTC);
  }

  /**
   * Converts a date string in ISO-8601 format with milliseconds (e.g. "2019-05-06T00:00:00.000Z") to an Instant.
   *
   * @param date The date string to convert.
   * @return The given date as an Instant.
   */
  public static Instant yyyyMMddTHHmmssSSSZToInstantToInstant(String date) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
      OffsetDateTime odt = OffsetDateTime.parse(date, formatter);
      return odt.toInstant();
  }
}
