package esthesis.edge.jobs;

import esthesis.edge.config.EdgeProperties;
import esthesis.edge.model.QueueItemEntity;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class PurgeJob {

  private final EdgeProperties edgeProperties;

  // The kind of checks that are performed while purging records.
  private enum PURGE_MODE {
    CORE, LOCAL, BOTH
  }

  private long purgeRecords(PURGE_MODE mode) {
    log.debug("Purging records in mode '{}'.", mode);
    long recordsPurged = 0;

    // Purge queued items that have been successfully delivered, based on the mode in which EDGE is
    // operating.
    recordsPurged = switch (mode) {
      case CORE -> QueueItemEntity.delete(
          "processedCoreAt IS NOT NULL AND processedCoreAt < ?1",
          Instant.now().minus(Duration.ofMinutes(edgeProperties.purgeSuccessfulMinutes())));
      case LOCAL -> QueueItemEntity.delete(
          "processedLocalAt IS NOT NULL AND processedLocalAt < ?1",
          Instant.now().minus(Duration.ofMinutes(edgeProperties.purgeSuccessfulMinutes())));
      case BOTH -> QueueItemEntity.delete(
          "processedLocalAt IS NOT NULL AND processedCoreAt IS NOT NULL"
              + " AND processedLocalAt < ?1 AND processedCoreAt < ?1",
          Instant.now().minus(Duration.ofMinutes(edgeProperties.purgeSuccessfulMinutes())));
    };

    // Purge any queued items that have been queued for longer than defined.
    recordsPurged += QueueItemEntity.delete("createdAt < ?1",
        Instant.now().minus(Duration.ofMinutes(edgeProperties.purgeQueuedMinutes())));

    return recordsPurged;
  }

  @Transactional
  @Scheduled(cron = "{esthesis.edge.purge-cron}")
  public void execute() {
    log.debug("Purging data started.");
    long recordsPurged = 0;

    // Decide on purge mode,
    if (edgeProperties.local().enabled() && edgeProperties.core().push().enabled()) {
      recordsPurged = purgeRecords(PURGE_MODE.BOTH);
    } else if (edgeProperties.local().enabled()) {
      recordsPurged = purgeRecords(PURGE_MODE.LOCAL);
    } else if (edgeProperties.core().push().enabled()) {
      recordsPurged = purgeRecords(PURGE_MODE.CORE);
    }

    log.debug("Purging data finished, purged '{}' records.", recordsPurged);
  }
}
