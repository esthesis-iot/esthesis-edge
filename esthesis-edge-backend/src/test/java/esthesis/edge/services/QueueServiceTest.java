package esthesis.edge.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import esthesis.edge.dto.QueueItemDTO;
import esthesis.edge.model.QueueItemEntity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Transactional
@QuarkusTest
class QueueServiceTest {

  @Inject
  QueueService queueService;

  @BeforeEach
  void setUp() {
    QueueItemEntity.deleteAll();
  }

  @Test
  void testQueue() {
    QueueItemDTO queueItemDTO = new QueueItemDTO();
    queueItemDTO.setId("test-id");
    queueItemDTO.setCreatedAt(Instant.now());
    queueItemDTO.setDataObject("data");
    queueItemDTO.setHardwareId("hardware-id");

    queueService.queue(queueItemDTO);

    Optional<QueueItemEntity> result = QueueItemEntity.findByIdOptional("test-id");
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  void testPeek() {
    QueueItemEntity queueItemEntity = new QueueItemEntity();
    queueItemEntity.setId("test-id");
    queueItemEntity.setCreatedAt(Instant.now());
    queueItemEntity.setDataObject("data");
    queueItemEntity.setHardwareId("hardware-id");
    queueItemEntity.persist();

    Optional<QueueItemDTO> result = queueService.peek();
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  void testRemove() {
    QueueItemEntity queueItemEntity = new QueueItemEntity();
    queueItemEntity.setId("test-id");
    queueItemEntity.setCreatedAt(Instant.now());
    queueItemEntity.setDataObject("data");
    queueItemEntity.setHardwareId("hardware-id");
    queueItemEntity.persist();

    queueService.remove("test-id");

    Optional<QueueItemEntity> result = QueueItemEntity.findByIdOptional("test-id");
    assertFalse(result.isPresent());
  }

  @Test
  void testMarkProcessedLocal() {
    QueueItemEntity queueItemEntity = new QueueItemEntity();
    queueItemEntity.setId("test-id");
    queueItemEntity.setHardwareId("hardware-id");
    queueItemEntity.setCreatedAt(Instant.now());
    queueItemEntity.setDataObject("data");
    queueItemEntity.persist();

    queueService.markProcessedLocal("test-id");

    queueItemEntity = QueueItemEntity.findById("test-id");
    assertNotNull(queueItemEntity.getProcessedLocalAt());
  }

  @Test
  void testMarkProcessedCore() {
    QueueItemEntity queueItemEntity = new QueueItemEntity();
    queueItemEntity.setId("test-id");
    queueItemEntity.setHardwareId("hardware-id");
    queueItemEntity.setCreatedAt(Instant.now());
    queueItemEntity.setDataObject("data");
    queueItemEntity.persist();

    queueService.markProcessedCore("test-id");

    queueItemEntity = QueueItemEntity.findById("test-id");
    assertNotNull(queueItemEntity.getProcessedCoreAt());
  }

  @Test
  void testList() {
    QueueItemEntity queueItemEntity = new QueueItemEntity();
    queueItemEntity.setId("test-id");
    queueItemEntity.setHardwareId("hardware-id");
    queueItemEntity.setCreatedAt(Instant.now());
    queueItemEntity.setDataObject("data");
    queueItemEntity.setHardwareId("hardware-id");
    queueItemEntity.persist();

    List<QueueItemDTO> result = queueService.list();
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }
}
