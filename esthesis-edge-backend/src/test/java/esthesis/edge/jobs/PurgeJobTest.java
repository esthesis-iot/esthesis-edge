package esthesis.edge.jobs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
    assertDoesNotThrow(() ->
        purgeJob.execute()
    );

    assertDoesNotThrow(() ->
        QueueItemEntity.builder()
            .id(UUID.randomUUID().toString())
            .hardwareId(UUID.randomUUID().toString())
            .processedCoreAt(null)
            .processedLocalAt(null)
            .createdAt(Instant.EPOCH)
            .dataObject("energy watt=1000")
            .build()
            .persist()
    );

    assertDoesNotThrow(() ->
        purgeJob.execute()
    );
  }
}
