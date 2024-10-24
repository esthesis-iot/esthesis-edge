package esthesis.edge.modules.enedis;

import esthesis.edge.modules.enedis.config.EnedisProperties;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class EnedisModule {

  private final EnedisProperties enedisProperties;

  public void onStart(@Observes StartupEvent ev) {
    if (enedisProperties.enabled()) {
      log.info("Enedis module is enabled.");
    }
  }
}
