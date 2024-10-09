package esthesis.edge.impl.job;

import esthesis.edge.api.util.EdgeProperties;
import esthesis.edge.impl.model.QueueItemEntity;
import esthesis.edge.impl.service.SyncService;
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

  private void syncInfluxDB() {
    QueueItemEntity.findNotProcessedLocal().forEach(queueItem -> {
      log.debug("Local syncing queue item '{}'.", queueItem.getId());
//      syncService.influxDBPost(queueItem);
//      queueItem.setProcessedLocalAt(Instant.now());
//      queueItem.persist();
    });
  }

  public void sync() {
    Instant start = Instant.now();
    log.debug("Syncing data started.");

    if (edgeProperties.local().enabled()) {
      syncService.syncInfluxDB();
    }

    log.debug("Syncing data finished in '{}' ms.",
        Instant.now().toEpochMilli() - start.toEpochMilli());
  }
}
