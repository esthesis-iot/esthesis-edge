package esthesis.edge.modules.fronius.config;

public class FroniusConstants {

    // Private constructor to hide the implicit public one.
    private FroniusConstants(){
    }

    // The name of the module.
    public static final String MODULE_NAME = "fronius";

    // Errors counter for 'power flow realtime data'.
    public static final String CONFIG_PFR_ERRORS = MODULE_NAME + "_pfr_errors";

}
