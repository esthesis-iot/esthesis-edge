package esthesis.edge.api.util;

import io.smallrye.config.ConfigMapping;
import java.util.List;
import java.util.Optional;

@ConfigMapping(prefix = "esthesis.edge")
public interface EdgeProperties {
  String adminSecret();
  Locale local();
  Core core();

  interface Locale {
    boolean enabled();
    InfluxDB influxDB();

    interface InfluxDB {
      String url();
      String token();
      String bucket();
      String org();
    }
  }

  interface Core {
    Push push();
    Registration registration();

    interface Push {
      boolean enabled();
    }

    interface Registration {
     boolean enabled();
     Optional<String> secret();
    }

    List<String> tags();
  }
}
