package esthesis.edge.modules.deddie.config;

public class DeddieConstants {

    // Private constructor to hide the implicit public one.
    private DeddieConstants() {
    }

    // The name of the module.
    public static final String MODULE_NAME = "deddie";

    // The access token associated with the device.
    public static final String CONFIG_ACCESS_TOKEN = "access_token";

    // The tax number associated with the device.
    public static final String CONFIG_TAX_NUMBER = "tax_number";

    // The supply number associated with the device.
    public static final String CONFIG_SUPPLY_NUMBER = "supply_number";

    // The last date 'curve active consumption' was fetched.
    public static final String CONFIG_CAC_LAST_FETCHED_AT = "cac_last_fetched_at";

    // Errors counter for 'curve active consumption'.
    public static final String CONFIG_CAC_ERRORS = "cac_errors";

    // The last date 'curve reactive power' was fetched.
    public static final String CONFIG_CRP_LAST_FETCHED_AT = "crp_last_fetched_at";

    // Errors counter for 'curve reactive power'.
    public static final String CONFIG_CRP_ERRORS = "crp_errors";

    // The last date 'curve energy produced' was fetched.
    public static final String CONFIG_CEP_LAST_FETCHED_AT = "cep_last_fetched_at";

    // Errors counter for 'curve energy produced'.
    public static final String CONFIG_CEP_ERRORS = "cep_errors";

    // The last date 'curve energy injected' was fetched.
    public static final String CONFIG_CEI_LAST_FETCHED_AT = "cei_last_fetched_at";

    // Errors counter for 'curve energy injected'.
    public static final String CONFIG_CEI_ERRORS = "cei_errors";

    // Class types for curves that can be fetched from the API.
    public static final String CURVES_CLASS_TYPE_CAC = "active"; // Curve Active Consumption
    public static final String CURVES_CLASS_TYPE_CRP = "reactive"; // Curve Reactive Power
    public static final String CURVES_CLASS_TYPE_CEP = "produced"; // Curve Energy Produced
    public static final String CURVES_CLASS_TYPE_CEI = "injected"; // Curve Energy Injected

    // The type of analysis for curves.
    // 1 = for 15-min intervals, 2 = for hourly, 3 = for daily, 4 = for monthly.
    public static final Integer CURVES_ANALYSIS_TYPE = 1;

    // API scope.
    public static final String API_SCOPE = "API";

}
