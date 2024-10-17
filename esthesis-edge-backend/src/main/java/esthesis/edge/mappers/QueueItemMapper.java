package esthesis.edge.mappers;

import esthesis.edge.dto.QueueItemDTO;
import esthesis.edge.model.QueueItemEntity;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Helper mappers for QueueItemEntity.
 */
@ApplicationScoped
public class QueueItemMapper {

  /**
   * Converts QueueItemDTO to QueueItemEntity.
   *
   * @param queueItemEntity the entity to convert.
   * @return the converted DTO.
   */
  public QueueItemDTO toDTO(QueueItemEntity queueItemEntity) {
    return QueueItemDTO.builder().id(queueItemEntity.getId())
        .dataObject(queueItemEntity.getDataObject()).createdAt(queueItemEntity.getCreatedAt())
        .processedLocalAt(queueItemEntity.getProcessedLocalAt())
        .processedCoreAt(queueItemEntity.getProcessedCoreAt())
        .hardwareId(queueItemEntity.getHardwareId()).build();
  }

  /**
   * Converts QueueItemEntity to QueueItemDTO.
   *
   * @param queueItemEntities the entities to convert.
   * @return the converted DTO.
   */
  public List<QueueItemDTO> toDTO(List<QueueItemEntity> queueItemEntities) {
    return queueItemEntities.stream().map(this::toDTO).toList();
  }
}
