package esthesis.edge.modules.fronius.config;

import io.smallrye.config.ConfigMapping;

import java.util.List;

/**
 * Configuration properties for the Fronius module.
 *
 * <p>These properties are read from the main {@code application.yaml} file.
 */
@ConfigMapping(prefix = "esthesis.edge.modules.fronius")
public interface FroniusProperties {

    // Whether the Fronius module is enabled.
    boolean enabled();

    // A Quartz cron expression to schedule how often data is fetched from Fronius Rest API.
    String cron();

    // The maximum number of Fronius devices that can be registered.
    int maxDevices();

    // The configuration of Frounius Solar API Endpoints supported.
    FetchTypes fetchTypes();

    interface FetchTypes {

        String toString();

        Pfr pfr();


        // Power flow realtime data Fronius Solar API endpoint configuration.
        interface Pfr {

            // Whether the prf endpoint is enabled.
            boolean enabled();

            // The number of errors that can be tolerated before the endpoint is disabled.
            int errorsThreshold();

            // The esthesis hardware ids for the inverters in the PFR response.
            // The order of the ids should match the order of the inverters in the response.
            List<String> invertersHardwareIds();

            Eday eday();

            // AC Energy for the day field configuration.
            interface Eday {

                // The category name of the data in eLP.
                String category();

                // The measurement name of the data in eLP.
                String measurement();
            }

        }
    }
}
