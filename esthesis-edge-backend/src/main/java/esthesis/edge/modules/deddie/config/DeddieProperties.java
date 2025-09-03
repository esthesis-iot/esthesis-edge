package esthesis.edge.modules.deddie.config;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;

/**
 * Configuration properties for the Deddie module.
 *
 * <p>These properties are read from the main {@code application.yaml} file.
 */
@ConfigMapping(prefix = "esthesis.edge.modules.deddie")
public interface DeddieProperties {

    // Whether the Deddie module is enabled.
    boolean enabled();

    // A Quartz cron expression to schedule how often data is fetched from Deddie Rest API.
    String cron();

    // The maximum number of Deddie devices that can be registered.
    int maxDevices();

    // The maximum number of days in the past that data can be fetched from. This is used in the
    // initialisation of a newly registered device. Subsequent fetches will be limited by the
    // date of the last fetch.
    int pastDaysInit();

    // The configuration of Deddie API Endpoints supported.
    FetchTypes fetchTypes();

    // Self-registration page configuration.
    SelfRegistration selfRegistration();

    interface FetchTypes {

        String toString();

        // Curve active consumption Deddie API endpoint configuration.
        Cac cac();

        interface Cac {

            // Whether the endpoint is enabled.
            boolean enabled();

            // The category name of the data in eLP.
            String category();

            // The measurement name of the data in eLP.
            String measurement();

            // The number of errors that can be tolerated before the endpoint is disabled.
            int errorsThreshold();


            String toString();
        }

        // Curve reactive power Deddie API endpoint configuration.
        Crp crp();

        interface Crp {

            // Whether the endpoint is enabled.
            boolean enabled();

            // The category name of the data in eLP.
            String category();

            // The measurement name of the data in eLP.
            String measurement();

            // The number of errors that can be tolerated before the endpoint is disabled.
            int errorsThreshold();


            String toString();
        }

        // Curve energy produced Deddie API endpoint configuration.
        Cep cep();

        interface Cep {

            // Whether the endpoint is enabled.
            boolean enabled();

            // The category name of the data in eLP.
            String category();

            // The measurement name of the data in eLP.
            String measurement();

            // The number of errors that can be tolerated before the endpoint is disabled.
            int errorsThreshold();

            String toString();
        }

        // Curve energy injected Deddie API endpoint configuration.
        Cei cei();

        interface Cei {

            // Whether the endpoint is enabled.
            boolean enabled();

            // The category name of the data in eLP.
            String category();

            // The measurement name of the data in eLP.
            String measurement();

            // The number of errors that can be tolerated before the endpoint is disabled.
            int errorsThreshold();


            String toString();
        }
    }

    interface SelfRegistration {
        // Whether self-registration page is enabled.
        boolean enabled();

        // Built-in pages configuration.
        Page page();

        String toString();

        interface Page {
            // The URL of logo #1.
            Optional<String> logo1Url();

            // The alt text of logo #1.
            Optional<String> logo1Alt();

            // The URL of logo #2.
            Optional<String> logo2Url();

            // The alt text of logo #2.
            Optional<String> logo2Alt();

            // The URL of logo #3.
            Optional<String> logo3Url();

            // The alt text of logo #3.
            Optional<String> logo3Alt();

            // Registration page configuration.
            Registration registration();

            // Success page configuration.
            Success success();

            // Error page configuration.
            Error error();

            String toString();

            interface Registration {

                // The title of the registration page.
                String title();

                // The message of the registration page.
                String message();

                // The placeholder message for the tax number input.
                String placeholderTaxNumber();

                // The placeholder message for the access token input.
                String placeholderAccessToken();

                // The placeholder message for the supply number input.
                String placeholderSupplyNumber();

                String toString();
            }

            interface Success {

                // The title of the success page.
                String title();

                // The message of the success page.
                String message();

                String toString();
            }

            interface Error {

                // The title of the error page.
                String title();

                // The message of the error page.
                String message();

                String toString();
            }

        }
    }
}
