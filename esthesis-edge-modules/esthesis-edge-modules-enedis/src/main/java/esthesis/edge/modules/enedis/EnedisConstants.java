package esthesis.edge.modules.enedis;

public class EnedisConstants {

  private EnedisConstants() {
  }

  public static final String MODULE_NAME = "enedis";

  // The RPM associated with the device.
  public static final String CONFIG_PRM = "prm";

  // The date at which the RPM was enabled.
  public static final String CONFIG_RPM_ENABLED_AT = "rpm_enabled_at";

  // The date at which the RPM expires.
  public static final String CONFIG_RPM_EXPIRES_AT = "rpm_expires_at";

  // The last date 'consommation quotidienne' was fetched.
  public static final String CONFIG_CQ_LAST_FETCHED_AT = "cq_last_fetched_at";

  // The last date 'puissance maximale de consommation' was fetched.
  public static final String CONFIG_PMC_LAST_FETCHED_AT = "pmc_last_fetched_at";

  // The last date 'courbe de consommation' was fetched.
  public static final String CONFIG_CC_LAST_FETCHED_AT = "cc_last_fetched_at";

  // The last date 'production quotidienne' was fetched.
  public static final String CONFIG_PQ_LAST_FETCHED_AT = "pq_last_fetched_at";

  // The last date 'courbe de production' was fetched.
  public static final String CONFIG_CP_LAST_FETCHED_AT = "cp_last_fetched_at";

}
