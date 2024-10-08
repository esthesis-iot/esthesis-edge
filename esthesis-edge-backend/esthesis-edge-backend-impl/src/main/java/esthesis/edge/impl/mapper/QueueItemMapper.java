package esthesis.edge.impl.mapper;

import esthesis.edge.api.dto.QueueItemDTO;
import esthesis.edge.impl.model.QueueItemEntity;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class QueueItemMapper {

  public QueueItemDTO toDTO(QueueItemEntity queueItemEntity) {
    return QueueItemDTO.builder()
        .id(queueItemEntity.getId())
        .dataObject(queueItemEntity.getDataObject())
        .createdAt(queueItemEntity.getCreatedAt())
        .processedAt(queueItemEntity.getProcessedAt())
        .hardwareId(queueItemEntity.getHardwareId())
        .build();
  }

  public List<QueueItemDTO> toDTO(List<QueueItemEntity> queueItemEntities) {
    return queueItemEntities.stream().map(this::toDTO).collect(Collectors.toList());
  }
}
