package esthesis.edge.impl.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import esthesis.edge.api.util.EdgeProperties;
import esthesis.edge.impl.model.QueueItemEntity;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SyncService {

  private final EdgeProperties edgeProperties;
  private InfluxDBClient influxDBClient;

  public void mqttPost() {

  }

  public void influxDBPost(List<QueueItemEntity> queueItem) {

  }

  public void syncInfluxDB() {
    influxDBClient = InfluxDBClientFactory.create(
        edgeProperties.local().influxDB().url(),
        edgeProperties.local().influxDB().token().toCharArray(),
        edgeProperties.local().influxDB().org(),
        edgeProperties.local().influxDB().bucket());

    QueueItemEntity.findNotProcessedLocal().forEach(queueItem -> {
      log.debug("Local syncing queue item '{}'.", queueItem.getId());
//      syncService.influxDBPost(queueItem);
      queueItem.setProcessedLocalAt(Instant.now());
      queueItem.persist();
    });

  }
}
