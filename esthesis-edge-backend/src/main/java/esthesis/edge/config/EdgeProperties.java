package esthesis.edge.config;

import io.smallrye.config.ConfigMapping;
import java.util.List;
import java.util.Optional;

/**
 * Edge runtime properties.
 */
@ConfigMapping(prefix = "esthesis.edge")
public interface EdgeProperties {

  String toString();

  // The value to send as Bearer token in the authorization header for requests to the admin API.
  String adminSecret();

  // A Quartz cron expression for the synchronization job (i.e. data to local InfluxDB and
  // esthesis CORE).
  String syncCron();

  // A Quartz cron expression for the purge job, deleting data from the queue table.
  String purgeCron();


  // The number of minutes to keep data successfully synchronised in the queue table before purging.
  int purgeSuccessfulMinutes();

  // The number of minutes to keep data in the queue table before purging.
  int purgeQueuedMinutes();

  // Local storage (InfluxDB) configuration.
  Locale local();

  // esthesis CORE configuration.
  Core core();

  interface Locale {

    String toString();

    // Whether data is synchronised to the InfluxDB.
    boolean enabled();

    // InfluxDB configuration.
    InfluxDB influxDB();

    interface InfluxDB {

      String toString();

      // The URL of the InfluxDB.
      String url();

      // The access token of the InfluxDB database.
      String token();

      // The name of the InfluxDB bucket.
      String bucket();

      // The name of the InfluxDB organization.
      String org();
    }
  }

  interface Core {

    String toString();

    // The certificate of the CA that has generated the certificated being used by the esthesis
    // CORE MQTT server.
    Optional<String> cert();

    // The algorithm used for the private key of devices.
    String keyAlgorithm();

    // esthesis CORE data synchronisation configuration.
    Push push();

    // Configuration on registering devices to esthesis CORE.
    Registration registration();

    interface Push {

      String toString();

      // Whether data is synchronised to the esthesis CORE.
      boolean enabled();

      // The URL of the esthesis CORE MQTT server to use.
      Optional<String> url();

      // The name of the topic to send telemetry data to.
      Optional<String> topicTelemetry();

      // The name of the topic to send ping data to.
      Optional<String> topicPing();
    }

    interface Registration {

      String toString();

      // Whether device registration is enabled (i.e. EDGE devices are registered to esthesis CORE).
      boolean enabled();

      // The shared secret used during device registration.
      Optional<String> secret();

      // A Quartz cron expression for the core registration job, registering devices to esthesis CORE.
      String cron();
    }

    // The tags to add to the device registration.
    List<String> tags();
  }
}
