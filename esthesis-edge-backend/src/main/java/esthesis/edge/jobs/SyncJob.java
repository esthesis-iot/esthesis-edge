package esthesis.edge.jobs;

import esthesis.edge.config.EdgeProperties;
import esthesis.edge.services.SyncService;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SyncJob {

  private final EdgeProperties edgeProperties;
  private final SyncService syncService;

  public void sync() {
    Instant start = Instant.now();
    log.debug("Syncing data started.");

    if (edgeProperties.local().enabled()) {
      syncService.syncInfluxDB();
    }

    if (edgeProperties.core().push().enabled()) {
      syncService.syncCore();
    }

    log.debug("Syncing data finished in '{}' ms.",
        Instant.now().toEpochMilli() - start.toEpochMilli());
  }
}
