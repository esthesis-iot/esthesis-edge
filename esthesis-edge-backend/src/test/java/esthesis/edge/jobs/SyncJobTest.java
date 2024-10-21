package esthesis.edge.jobs;

import esthesis.edge.model.QueueItemEntity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Transactional
class SyncJobTest {

  @Inject
  SyncJob syncJob;

  @Test
  void execute() {
    syncJob.execute();

    QueueItemEntity.builder()
        .id(UUID.randomUUID().toString())
        .hardwareId(UUID.randomUUID().toString())
        .processedCoreAt(null)
        .processedLocalAt(null)
        .createdAt(Instant.EPOCH)
        .dataObject("test")
        .build()
        .persist();

    syncJob.execute();
  }
}
