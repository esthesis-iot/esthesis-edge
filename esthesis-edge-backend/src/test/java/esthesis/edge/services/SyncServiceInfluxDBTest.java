package esthesis.edge.services;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import esthesis.edge.dto.QueueItemDTO;
import esthesis.edge.model.QueueItemEntity;
import esthesis.edge.testcontainers.InfluxDBTC;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Transactional
@QuarkusTestResource(value = InfluxDBTC.class, restrictToAnnotatedClass = true)
class SyncServiceInfluxDBTest {

  @InjectSpy
  SyncService syncService;

  @Inject
  QueueService queueService;

  private QueueItemDTO createTestQueueItem(String id, String dataObject) {
    QueueItemDTO queueItem = new QueueItemDTO();
    queueItem.setId(id);
    queueItem.setHardwareId("test");
    queueItem.setDataObject(dataObject);
    queueItem.setProcessedLocalAt(null);
    queueItem.setProcessedCoreAt(null);
    queueItem.setCreatedAt(Instant.now());

    return queueItem;
  }

  @Test
  void syncInfluxDB() {
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
      QueueItemDTO item = createTestQueueItem(id, data);
      queueService.queue(item);
      syncService.syncInfluxDB();
      assertNotNull(((QueueItemEntity) (QueueItemEntity.findById(id))).getProcessedLocalAt());
    }
  }

  @Test
  void syncCore() {
  }

}
