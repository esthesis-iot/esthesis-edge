package esthesis.edge.services;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import esthesis.edge.TestUtils;
import esthesis.edge.dto.QueueItemDTO;
import esthesis.edge.model.QueueItemEntity;
import esthesis.edge.testcontainers.HiveMQTC;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Transactional
@QuarkusTestResource(value = HiveMQTC.class, restrictToAnnotatedClass = true)
class SyncServiceCoreTest {

  @Inject
  SyncService syncService;

  @Inject
  QueueService queueService;

  @Inject
  TestUtils testUtils;

  @Test
  void syncCore() {
    testUtils.createDevice("test");

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
      QueueItemDTO item = testUtils.createQueueItem(id, "test", data);
      queueService.queue(item);
      syncService.syncCore();
      assertNotNull(((QueueItemEntity) (QueueItemEntity.findById(id))).getProcessedCoreAt());
    }
  }


  @Test
  void syncCoreSkipped() {
    // Create a device without core registration which should be skipped during sync.
    testUtils.createDeviceWithoutCoreRegistration("test2");

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
      String id = UUID.randomUUID().toString();
      QueueItemDTO item = testUtils.createQueueItem(id, "test2", data);
      queueService.queue(item);
      syncService.syncCore();
      assertNull(((QueueItemEntity) (QueueItemEntity.findById(id))).getProcessedCoreAt());
    }
  }

}
