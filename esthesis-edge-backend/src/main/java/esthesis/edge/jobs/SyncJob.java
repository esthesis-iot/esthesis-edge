package esthesis.edge.jobs;

import esthesis.common.exception.QProcessingException;
import esthesis.edge.config.EdgeProperties;
import esthesis.edge.services.SyncService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sync job synchronises data in queue_item database table with a local InfluxDB and/or esthesis
 * CORE.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SyncJob {

  private final EdgeProperties edgeProperties;
  private final SyncService syncService;

  /**
   * Executes the sync job.
   */
  @Scheduled(cron = "{esthesis.edge.sync-cron}")
  public void execute() {
    Instant start = Instant.now();
    log.debug("Syncing data started.");

    if (edgeProperties.local().enabled() && !syncService.syncInfluxDB()) {
      throw new QProcessingException("Failed to sync data to InfluxDB.");
    }

    if (edgeProperties.core().push().enabled() && !syncService.syncCore()) {
      throw new QProcessingException("Failed to sync data to esthesis CORE.");
    }

    log.debug("Syncing data finished in '{}' ms.",
        Instant.now().toEpochMilli() - start.toEpochMilli());
  }
}
