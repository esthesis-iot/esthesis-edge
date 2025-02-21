package esthesis.edge.services;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import esthesis.common.avro.AvroUtils;
import esthesis.common.avro.PayloadData;
import esthesis.common.avro.ValueData;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.common.exception.QMismatchException;
import esthesis.common.exception.QValueIsRequiredException;
import esthesis.edge.clients.MqttPublisher;
import esthesis.edge.config.EdgeProperties;
import esthesis.edge.model.DeviceEntity;
import esthesis.edge.model.QueueItemEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Service class for syncing data between the edge and InfluxDB and esthesis CORE.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SyncService {

  private final EdgeProperties edgeProperties;
  private final AvroUtils avroUtils;
  private InfluxDBClient influxDBClient;

  /**
   * Post a queue item to InfluxDB.
   *
   * @param queueItemEntity The queue item to post.
   */
  private void influxDBPost(QueueItemEntity queueItemEntity) {
    log.debug("InfluxDB syncing queue item '{}'.", queueItemEntity.getId());

    // Prepare InfluxDB point by splitting the payload data.
    String[] dataList = queueItemEntity.getDataObject().split("\n");
    for (String data : dataList) {
      PayloadData payloadData = avroUtils.parsePayload(data);
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
          case INTEGER ->
              point.addField(valueData.getName(), Integer.parseInt(valueData.getValue()));
          case LONG, BIG_INTEGER ->
              point.addField(valueData.getName(), Long.parseLong(valueData.getValue()));
          case FLOAT -> point.addField(valueData.getName(), Float.parseFloat(valueData.getValue()));
          case DOUBLE, BIG_DECIMAL ->
              point.addField(valueData.getName(), Double.parseDouble(valueData.getValue()));
          case UNKNOWN -> log.warn("Unknown value type '{}'.", valueData.getValueType());
        }

        // Write point to InfluxDB.
        WriteApiBlocking writeApiBlocking = influxDBClient.getWriteApiBlocking();
        log.debug("InfluxDB writing point '{}'.", point.toLineProtocol());
        writeApiBlocking.writePoint(point);
        log.debug("InfluxDB synced queue item '{}'.", queueItemEntity.getId());
      }
    }
  }

  /**
   * Post queue items to esthesis CORE via MQTT.
   *
   * @param deviceEntity      The device to post the queue items for.
   * @param queueItemEntities The queue items to post.
   * @throws MqttException If an error occurs while posting the queue items.
   */
  private void mqttPost(DeviceEntity deviceEntity, List<QueueItemEntity> queueItemEntities)
  throws MqttException {
    String mqttUrl = edgeProperties.core().push().url().orElseThrow(
        () -> new QValueIsRequiredException("esthesis CORE MQTT server is not " + "specified."));
    String telemetryTopic = edgeProperties.core().push().topicTelemetry().orElseThrow(
        () -> new QValueIsRequiredException(
            "esthesis CORE MQTT telemetry topic is " + "not specified.")) + "/"
        + deviceEntity.getHardwareId();
    MqttPublisher mqttPublisher = new MqttPublisher(mqttUrl);

    try {
      if (mqttUrl.startsWith("ssl://")) {
        mqttPublisher.connect(new String(Base64.getDecoder().decode(edgeProperties.core().cert()
                .orElseThrow(
                    () -> new QDoesNotExistException("esthesis CORE certificate not found.")))),
            deviceEntity.getCertificate(), deviceEntity.getPrivateKey(),
            edgeProperties.core().keyAlgorithm());
      } else if (mqttUrl.startsWith("tcp://")) {
        mqttPublisher.connect();
      } else {
        throw new QMismatchException("esthesis CORE MQTT server URL is invalid, it should start "
            + "with 'ssl://' or 'tcp://'.");
      }

      for (QueueItemEntity queueItemEntity : queueItemEntities) {
        try {
          log.debug("esthesis CORE syncing queue item '{}'.", queueItemEntity.getId());
          mqttPublisher.publish(telemetryTopic, queueItemEntity.getDataObject());
          queueItemEntity.setProcessedCoreAt(Instant.now());
          queueItemEntity.persist();
          log.debug("esthesis CORE synced queue item '{}'.", queueItemEntity.getId());
        } catch (MqttException e) {
          log.error("Error syncing queue item '{}'.", queueItemEntity.getId(), e);
        }
      }
      String pingTopic = edgeProperties.core().push().topicPing().orElseThrow(
          () -> new QValueIsRequiredException(
              "esthesis CORE MQTT ping topic is not " + "specified.")) + "/"
          + deviceEntity.getHardwareId();
      mqttPublisher.publish(pingTopic, "health ping=" + Instant.now().toString());
    } catch (Exception e) {
      log.error("Error syncing esthesis CORE.", e);
    } finally {
      mqttPublisher.disconnect();
    }
  }

  /**
   * Sync data between edge and InfluxDB.
   *
   * @return True if all entries could be processed, false otherwise.
   */
  @Transactional
  public boolean syncInfluxDB() {
    final AtomicBoolean hasErrors = new AtomicBoolean(false);
    influxDBClient = InfluxDBClientFactory.create(edgeProperties.local().influxDB().url(),
        edgeProperties.local().influxDB().token().toCharArray(),
        edgeProperties.local().influxDB().org(), edgeProperties.local().influxDB().bucket());

    log.debug("InfluxDB syncing started.");

    try {
      QueueItemEntity.findNotProcessedLocal().forEach(queueItem -> {
        log.debug("Local syncing queue item '{}'.", queueItem.getId());
        try {
          influxDBPost(queueItem);
          log.debug("Marking queue item '{}' as processed.", queueItem.getId());
          queueItem.setProcessedLocalAt(Instant.now());
          queueItem.persist();
        } catch (Exception e) {
          log.error("Error syncing queue item '{}'.", queueItem.getId(), e);
          hasErrors.set(true);
        }
      });
    } catch (Exception e) {
      log.error("Error syncing InfluxDB.", e);
      hasErrors.set(true);
    } finally {
      influxDBClient.close();
    }

    log.debug("InfluxDB syncing finished.");

    return !hasErrors.get();
  }

  /**
   * Sync data between the edge and esthesis CORE.
   *
   * @return True if all entries could be processed, false otherwise.
   */
  @Transactional
  public boolean syncCore() {
    final AtomicBoolean hasErrors = new AtomicBoolean(false);
    log.debug("esthesis CORE syncing started.");

    // Find distinct hardwareIds with unprocessed CORE items.
    List<String> hardwareIdToProcess = QueueItemEntity.findDistinctHardwareIdsWithNotProcessedCore();

    // For each hardwareId, fetch and process unprocessed CORE items.
    hardwareIdToProcess.forEach(hardwareId -> {
      log.debug("esthesis CORE syncing hardwareId '{}'.", hardwareId);
      List<QueueItemEntity> itemsToPost = QueueItemEntity.findByHardwareIdNotProcessedCore(
              hardwareId);
      try {
        DeviceEntity device = DeviceEntity.findByHardwareId(hardwareId).orElseThrow();
        // Check if the device is registered with esthesis CORE before syncing.
        if (device.getCoreRegisteredAt() != null) {
          mqttPost(device, itemsToPost);
          log.debug("esthesis CORE synced hardwareId '{}'.", hardwareId);
        } else {
          log.debug("Device with hardwareId '{}' is not registered with esthesis CORE, skipping sync.", hardwareId);
        }

      } catch (Exception e) {
        log.error("Error syncing hardwareId '{}'.", hardwareId, e);
        hasErrors.set(true);
      }
    });

    log.debug("esthesis CORE syncing finished.");
    return !hasErrors.get();
  }

}
