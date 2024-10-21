package esthesis.edge.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import esthesis.edge.dto.QueueItemDTO;
import esthesis.edge.model.QueueItemEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class QueueItemMapperTest {

  @Test
  void toDTOFromEntity() {
    QueueItemEntity queueItemEntity = new QueueItemEntity();
    queueItemEntity.setId(UUID.randomUUID().toString());
    queueItemEntity.setDataObject("dataObject");
    queueItemEntity.setCreatedAt(Instant.now());
    queueItemEntity.setProcessedLocalAt(Instant.now().plusSeconds(60));
    queueItemEntity.setProcessedCoreAt(Instant.now().plusSeconds(120));
    queueItemEntity.setHardwareId("hardwareId");

    QueueItemMapper queueItemMapper = new QueueItemMapper();
    QueueItemDTO queueItemDTO = queueItemMapper.toDTO(queueItemEntity);

    assertNotNull(queueItemDTO);
    assertEquals(queueItemEntity.getId(), queueItemDTO.getId());
    assertEquals(queueItemEntity.getDataObject(), queueItemDTO.getDataObject());
    assertEquals(queueItemEntity.getCreatedAt(), queueItemDTO.getCreatedAt());
    assertEquals(queueItemEntity.getProcessedLocalAt(), queueItemDTO.getProcessedLocalAt());
    assertEquals(queueItemEntity.getProcessedCoreAt(), queueItemDTO.getProcessedCoreAt());
    assertEquals(queueItemEntity.getHardwareId(), queueItemDTO.getHardwareId());
  }

  @Test
  void toDTOFromEntityList() {
    QueueItemEntity queueItemEntity = new QueueItemEntity();
    queueItemEntity.setId(UUID.randomUUID().toString());
    queueItemEntity.setDataObject("dataObject");
    queueItemEntity.setCreatedAt(Instant.now());
    queueItemEntity.setProcessedLocalAt(Instant.now().plusSeconds(60));
    queueItemEntity.setProcessedCoreAt(Instant.now().plusSeconds(120));
    queueItemEntity.setHardwareId("hardwareId");

    QueueItemMapper queueItemMapper = new QueueItemMapper();
    List<QueueItemDTO> queueItemDTO = queueItemMapper.toDTO(List.of(queueItemEntity,
        queueItemEntity));

    assertNotNull(queueItemDTO);
    assertEquals(2, queueItemDTO.size());
    assertEquals(queueItemEntity.getId(), queueItemDTO.get(0).getId());
    assertEquals(queueItemEntity.getDataObject(), queueItemDTO.get(0).getDataObject());
    assertEquals(queueItemEntity.getCreatedAt(), queueItemDTO.get(0).getCreatedAt());
    assertEquals(queueItemEntity.getProcessedLocalAt(), queueItemDTO.get(0).getProcessedLocalAt());
    assertEquals(queueItemEntity.getProcessedCoreAt(), queueItemDTO.get(0).getProcessedCoreAt());
    assertEquals(queueItemEntity.getHardwareId(), queueItemDTO.get(0).getHardwareId());

    assertEquals(queueItemEntity.getId(), queueItemDTO.get(1).getId());
    assertEquals(queueItemEntity.getDataObject(), queueItemDTO.get(1).getDataObject());
    assertEquals(queueItemEntity.getCreatedAt(), queueItemDTO.get(1).getCreatedAt());
    assertEquals(queueItemEntity.getProcessedLocalAt(), queueItemDTO.get(1).getProcessedLocalAt());
    assertEquals(queueItemEntity.getProcessedCoreAt(), queueItemDTO.get(1).getProcessedCoreAt());
    assertEquals(queueItemEntity.getHardwareId(), queueItemDTO.get(1).getHardwareId());
  }
}
