package esthesis.edge.jobs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import esthesis.edge.services.SyncService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Transactional
class SyncJobTest {

  @Inject
  SyncJob syncJob;

  @InjectSpy
  SyncService syncService;

  @Test
  void execute() {
    when(syncService.syncCore()).thenReturn(true);
    when(syncService.syncInfluxDB()).thenReturn(true);

    assertDoesNotThrow(() ->
        syncJob.execute()
    );
  }
}
