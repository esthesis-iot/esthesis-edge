package esthesis.edge.modules.enedis.config;

import io.smallrye.config.ConfigMapping;
import java.util.Optional;

/**
 * Configuration properties for the Enedis module.
 *
 * <p>These properties are read from the main {@code application.properties} file.
 */
@ConfigMapping(prefix = "esthesis.edge.modules.enedis")
public interface EnedisProperties {

  // Whether the Enedis module is enabled.
  boolean enabled();

  // A Quartz cron expression to schedule how often data is fetched from Enedis.
  String cron();

  // The maximum number of Enedis devices that can be registered.
  int maxDevices();

  // The maximum number of days in the past that data can be fetched from. This is used in the
  // initialisation of a newly registered device. Subsequent fetches will be limited by the
  // date of the last fetch.
  int pastDaysInit();

  // The ENEDIS API client id.
  String clientId();

  // The ENEDIS API client secret.
  String clientSecret();

  // Self-registration page configuration.
  SelfRegistration selfRegistration();

  // The configuration of Enedis API Endpoints supported.
  FetchTypes fetchTypes();

  interface FetchTypes {

    String toString();

    // Daily consumption Enedis API endpoint configuration.
    Dc dc();

    interface Dc {

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

    // Daily production Enedis API endpoint configuration.
    Dp dp();

    interface Dp {

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

    // Daily consumption maximum power Enedis API endpoint configuration.
    Dcmp dcmp();

    interface Dcmp {

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

    // Whether API state checking is enabled.
    boolean stateChecking();

    // The URL of the self-registration page. If left empty, a built-in page is used.
    Optional<String> welcomeUrl();

    // The URL of the redirect after accepting access to the device data in Enedis.
    String redirectUrl();

    // The duration for which data access is requested.
    String duration();

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

      // The URL of the accept button.
      String buttonUrl();

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

  String toString();
}
