package esthesis.edge.services;

import esthesis.edge.dto.QueueItemDTO;
import esthesis.edge.mappers.QueueItemMapper;
import esthesis.edge.model.QueueItemEntity;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class QueueService {

  private final QueueItemMapper queueItemMapper;

  public void queue(QueueItemDTO queueItemDTO) {
    QueueItemEntity queueItemEntity = new QueueItemEntity();
    queueItemEntity.setId(queueItemDTO.getId());
    queueItemEntity.setCreatedAt(queueItemDTO.getCreatedAt());
    queueItemEntity.setDataObject(queueItemDTO.getDataObject());
    queueItemEntity.setHardwareId(queueItemDTO.getHardwareId());
    queueItemEntity.persist();
  }

  public Optional<QueueItemDTO> peek() {
    Optional<QueueItemEntity> queueItem = QueueItemEntity.find("order by createdAt asc")
        .firstResultOptional();
    return queueItem.map(queueItemMapper::toDTO);
  }

  public void remove(String queueItemId) {
    QueueItemEntity.deleteById(queueItemId);
  }

  public void markProcessedLocal(String queueItemId) {
    QueueItemEntity queueItemEntity = QueueItemEntity.findById(queueItemId);
    queueItemEntity.setProcessedLocalAt(Instant.now());
    queueItemEntity.persist();
  }

  public void markProcessedCore(String queueItemId) {
    QueueItemEntity queueItemEntity = QueueItemEntity.findById(queueItemId);
    queueItemEntity.setProcessedCoreAt(Instant.now());
    queueItemEntity.persist();
  }

  public List<QueueItemDTO> list() {
    return queueItemMapper.toDTO(QueueItemEntity.listAll(Sort.by("createdAt")));
  }
}
