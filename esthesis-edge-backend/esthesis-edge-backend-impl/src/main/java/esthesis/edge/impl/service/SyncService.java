package esthesis.edge.impl.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import esthesis.common.avro.AvroUtils;
import esthesis.common.avro.PayloadData;
import esthesis.common.avro.ValueData;
import esthesis.common.exception.QMismatchException;
import esthesis.common.exception.QValueIsRequiredException;
import esthesis.edge.api.util.EdgeProperties;
import esthesis.edge.impl.client.MqttPublisher;
import esthesis.edge.impl.model.QueueItemEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SyncService {

  private final EdgeProperties edgeProperties;
  private InfluxDBClient influxDBClient;
  private final AvroUtils avroUtils;

  /**
   * Post a queue item to InfluxDB.
   *
   * @param queueItemEntity The queue item to post.
   */
  private void influxDBPost(QueueItemEntity queueItemEntity) {
    log.debug("InfluxDB syncing queue item '{}'.", queueItemEntity.getId());

    // Prepare InfluxDB point.
    PayloadData payloadData = avroUtils.parsePayload(queueItemEntity.getDataObject());
    Point point = Point.measurement(payloadData.getCategory())
        .addTag("hardwareId", queueItemEntity.getHardwareId())
        .time(Instant.parse(payloadData.getTimestamp()), WritePrecision.S);
    for (ValueData valueData : payloadData.getValues()) {
      switch (valueData.getValueType()) {
        case STRING -> point.addField(valueData.getName(), valueData.getValue());
        case BOOLEAN ->
            point.addField(valueData.getName(), Boolean.parseBoolean(valueData.getValue()));
        case BYTE -> point.addField(valueData.getName(), Byte.parseByte(valueData.getValue()));
        case SHORT -> point.addField(valueData.getName(), Short.parseShort(valueData.getValue()));
        case INTEGER -> point.addField(valueData.getName(), Integer.parseInt(valueData.getValue()));
        case LONG, BIG_INTEGER ->
            point.addField(valueData.getName(), Long.parseLong(valueData.getValue()));
        case FLOAT -> point.addField(valueData.getName(), Float.parseFloat(valueData.getValue()));
        case DOUBLE, BIG_DECIMAL ->
            point.addField(valueData.getName(), Double.parseDouble(valueData.getValue()));
        case UNKNOWN -> log.warn("Unknown value type '{}'.", valueData.getValueType());
      }

      // Write point to InfluxDB.
      try (WriteApi writeApi = influxDBClient.makeWriteApi()) {
        log.debug("InfluxDB writing point '{}'.", point.toLineProtocol());
        writeApi.writePoint(point);
      }
      log.debug("InfluxDB synced queue item '{}'.", queueItemEntity.getId());
    }
  }

  public void mqttPost(String hardwareId, List<QueueItemEntity> queueItemEntities)
  throws MqttException {
    String mqttUrl = edgeProperties.core().push().url()
        .orElseThrow(() -> new QValueIsRequiredException("esthesis CORE MQTT server is not "
            + "specified."));
    String topic = edgeProperties.core().push().topicTelemetry()
        .orElseThrow(() -> new QValueIsRequiredException("esthesis CORE MQTT topic is not "
            + "specified.")) + "/" + hardwareId;
    MqttPublisher mqttPublisher = new MqttPublisher(mqttUrl);

    try {
      if (mqttUrl.startsWith("ssl://")) {

      } else if (mqttUrl.startsWith("tcp://")) {
        mqttPublisher.connect();
      } else {
        throw new QMismatchException("esthesis CORE MQTT server URL is invalid, it should start "
            + "with 'ssl://' or 'tcp://'.");
      }

      for (QueueItemEntity queueItemEntity : queueItemEntities) {
        try {
          log.debug("esthesis CORE syncing queue item '{}'.", queueItemEntity.getId());
          mqttPublisher.publish(topic, queueItemEntity.getDataObject());
          queueItemEntity.setProcessedCoreAt(Instant.now());
          queueItemEntity.persist();
          log.debug("esthesis CORE synced queue item '{}'.", queueItemEntity.getId());
        } catch (Exception e) {
          log.error("Error syncing queue item '{}'.", queueItemEntity.getId(), e);
        }
      }
    } catch (Exception e) {
      log.error("Error syncing esthesis CORE.", e);
    } finally {
      mqttPublisher.disconnect();
    }
  }

  @Transactional
  public void syncInfluxDB() {
    influxDBClient = InfluxDBClientFactory.create(edgeProperties.local().influxDB().url(),
        edgeProperties.local().influxDB().token().toCharArray(),
        edgeProperties.local().influxDB().org(), edgeProperties.local().influxDB().bucket());

    log.debug("InfluxDB syncing started.");

    try {
      QueueItemEntity.findNotProcessedLocal().forEach(queueItem -> {
        log.debug("Local syncing queue item '{}'.", queueItem.getId());
        try {
          influxDBPost(queueItem);
          queueItem.setProcessedLocalAt(Instant.now());
          queueItem.persist();
        } catch (Exception e) {
          log.error("Error syncing queue item '{}'.", queueItem.getId(), e);
        }
      });
    } catch (Exception e) {
      log.error("Error syncing InfluxDB.", e);
    } finally {
      influxDBClient.close();
    }

    log.debug("InfluxDB syncing finished.");
  }

  @Transactional
  public void syncCore() {
    log.debug("esthesis CORE syncing started.");

    // Find distinct hardwareIds with unprocessed CORE items.
    List<String> hardwareIdToProcess = QueueItemEntity.findDistinctHardwareIdsWithNotProcessedCore();

    // For each hardwareId, fetch and process unprocessed CORE items.
    hardwareIdToProcess.forEach(hardwareId -> {
      log.debug("esthesis CORE syncing hardwareId '{}'.", hardwareId);
      List<QueueItemEntity> itemsToPost = QueueItemEntity.findByHardwareIdNotProcessedCore(
          hardwareId);
      try {
        mqttPost(hardwareId, itemsToPost);
//        itemsToPost.forEach(queueItem -> {
//          queueItem.setProcessedCoreAt(Instant.now());
//          queueItem.persist();
//        });
        log.debug("esthesis CORE synced hardwareId '{}'.", hardwareId);
      } catch (Exception e) {
        log.error("Error syncing hardwareId '{}'.", hardwareId, e);
      }
    });

    log.debug("esthesis CORE syncing finished.");
  }
}
