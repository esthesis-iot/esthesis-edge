package esthesis.edge.modules.enedis.config;

/**
 * Constants for the Enedis module.
 */
public class EnedisConstants {

  private EnedisConstants() {
  }

  // The name of the module.
  public static final String MODULE_NAME = "enedis";

  // The RPM associated with the device.
  public static final String CONFIG_PRM = "prm";

  // The date at which the RPM was enabled.
  public static final String CONFIG_PMR_ENABLED_AT = "pmr_enabled_at";

  // The date at which the RPM expires.
  public static final String CONFIG_PMR_EXPIRES_AT = "pmr_expires_at";

  // The last date 'daily consumption' was fetched.
  public static final String CONFIG_DC_LAST_FETCHED_AT = "dc_last_fetched_at";

  // The last date 'daily consumption max power' was fetched.
  public static final String CONFIG_DCMP_LAST_FETCHED_AT = "dcmp_last_fetched_at";

  // The last date 'daily production' was fetched.
  public static final String CONFIG_DP_LAST_FETCHED_AT = "dp_last_fetched_at";

  // Errors counter for 'daily consumption'.
  public static final String CONFIG_DC_ERRORS = "dc_errors";

  // Errors counter for 'daily consumption max power'.
  public static final String CONFIG_DCMP_ERRORS = "dcmp_errors";

  // Errors counter for 'daily production'.
  public static final String CONFIG_DP_ERRORS = "dp_errors";

  // The maximum number of requests per seconds Enedis API allows.
  public static final int REQUESTS_PER_SECOND = 10;

  // The maximum number of requests per hour Enedis API allows.
  public static final int REQUESTS_PER_HOUR = 10000;
}
