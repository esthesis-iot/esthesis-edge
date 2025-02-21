package esthesis.edge.services;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import esthesis.edge.dto.QueueItemDTO;
import esthesis.edge.model.DeviceEntity;
import esthesis.edge.model.QueueItemEntity;
import esthesis.edge.testcontainers.HiveMQSSLTC;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Transactional
@QuarkusTestResource(value = HiveMQSSLTC.class, restrictToAnnotatedClass = true)
class SyncServiceCoreSSLTest {

  @InjectSpy
  SyncService syncService;

  @Inject
  QueueService queueService;

  private QueueItemDTO createTestQueueItem(String id, String hardwareId, String dataObject) {
    QueueItemDTO queueItem = new QueueItemDTO();
    queueItem.setId(id);
    queueItem.setHardwareId(hardwareId);
    queueItem.setDataObject(dataObject);
    queueItem.setProcessedLocalAt(null);
    queueItem.setProcessedCoreAt(null);
    queueItem.setCreatedAt(Instant.now());

    return queueItem;
  }

  private void createDevice(String hardwareId) throws IOException {
    DeviceEntity deviceEntity = new DeviceEntity();
    deviceEntity.setId(UUID.randomUUID().toString());
    deviceEntity.setHardwareId(hardwareId);
    deviceEntity.setModuleName("test");
    deviceEntity.setEnabled(true);
    deviceEntity.setCreatedAt(Instant.now());
    deviceEntity.setCoreRegisteredAt(Instant.now());
    deviceEntity.setCertificate(HiveMQSSLTC.loadResourceFileAsText("mqtt/client.crt"));
    deviceEntity.setPrivateKey(HiveMQSSLTC.loadResourceFileAsText("mqtt/client.key"));
    deviceEntity.persist();
  }

  @Test
  void syncCore() throws IOException {
    createDevice("test");

    String[] dataList = {
        "energy c1=10",
        "energy c2=10,p1=5",
        "energy c3=10i,p2=5f",
        "energy c4=10\nenergy p3=10",
        "energy c5=10 2022-01-01T01:02:03Z",
        "energy c6=10,p4=5 2022-01-01T01:02:03Z",
        "energy c7=10i,p5=5f 2022-01-01T01:02:03Z",
        "energy c8=10\nenergy c9=10 2022-01-01T01:02:03Z",
        "energy t1='10',t2=true,t3=3s,t4=2d,t5=6l",
    };

    for (String data : dataList) {
      System.out.println("Testing: " + data);
      String id = UUID.randomUUID().toString();
      QueueItemDTO item = createTestQueueItem(id, "test", data);
      queueService.queue(item);
      syncService.syncCore();
      assertNotNull(((QueueItemEntity) (QueueItemEntity.findById(id))).getProcessedCoreAt());
    }
  }

}
