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
class PurgeJobTest {

  @Inject
  PurgeJob purgeJob;

  @Test
  void execute() {
    purgeJob.execute();

    QueueItemEntity.builder()
        .id(UUID.randomUUID().toString())
        .hardwareId(UUID.randomUUID().toString())
        .processedCoreAt(null)
        .processedLocalAt(null)
        .createdAt(Instant.EPOCH)
        .dataObject("test")
        .build()
        .persist();

    purgeJob.execute();
  }
}
