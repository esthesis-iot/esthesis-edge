package esthesis.edge.modules.enedis;

import io.smallrye.config.ConfigMapping;
import java.util.Optional;

@ConfigMapping(prefix = "esthesis.edge.modules.enedis")
public interface EnedisProperties {
  boolean enabled();
  String cron();
  int maxDevices();
  String clientId();
  String clientSecret();
  SelfRegistration selfRegistration();
  boolean fetchImmediately();

  interface SelfRegistration {
    boolean enabled();
    boolean stateChecking();
    Optional<String> welcomeUrl();
    String redirectUrl();
    String duration();
    Page page();

    interface Page {
      Optional<String> logo1Url();
      Optional<String> logo1Alt();
      Optional<String> logo2Url();
      Optional<String> logo2Alt();
      Registration registration();
      Success success();
      Error error();

      interface Registration {
        String title();
        String message();
      }

      interface Success {
        String title();
        String message();
      }

      interface Error {
        String title();
        String message();
      }
    }
  }
}
