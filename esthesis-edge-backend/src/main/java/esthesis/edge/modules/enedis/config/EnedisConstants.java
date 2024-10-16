package esthesis.edge.modules.enedis.config;

public class EnedisConstants {

  private EnedisConstants() {
  }

  public static final String MODULE_NAME = "enedis";

  // The RPM associated with the device.
  public static final String CONFIG_PRM = "prm";

  // The date at which the RPM was enabled.
  public static final String CONFIG_PMR_ENABLED_AT = "pmr_enabled_at";

  // The date at which the RPM expires.
  public static final String CONFIG_PMR_EXPIRES_AT = "pmr_expires_at";

  // The last date 'daily consumption' was fetched.
  public static final String CONFIG_DC_LAST_FETCHED_AT = "dc_last_fetched_at";

  // The last date 'puissance maximale de consommation' was fetched.
  public static final String CONFIG_PMC_LAST_FETCHED_AT = "pmc_last_fetched_at";

  // The last date 'courbe de consommation' was fetched.
  public static final String CONFIG_CC_LAST_FETCHED_AT = "cc_last_fetched_at";

  // The last date 'production quotidienne' was fetched.
  public static final String CONFIG_PQ_LAST_FETCHED_AT = "pq_last_fetched_at";

  // The last date 'courbe de production' was fetched.
  public static final String CONFIG_CP_LAST_FETCHED_AT = "cp_last_fetched_at";

}
