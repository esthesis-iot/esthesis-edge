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

/**
 * Service class for managing queued items.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class QueueService {

  private final QueueItemMapper queueItemMapper;

  /**
   * Queues an item.
   *
   * @param queueItemDTO The item to queue.
   */
  public void queue(QueueItemDTO queueItemDTO) {
    QueueItemEntity queueItemEntity = new QueueItemEntity();
    queueItemEntity.setId(queueItemDTO.getId());
    queueItemEntity.setCreatedAt(queueItemDTO.getCreatedAt());
    queueItemEntity.setDataObject(queueItemDTO.getDataObject());
    queueItemEntity.setHardwareId(queueItemDTO.getHardwareId());
    queueItemEntity.persist();
  }

  /**
   * Peeks at the next item in the queue without removing it. The next item is the oldest item in
   * the queue.
   *
   * @return The next item in the queue, if present.
   */
  public Optional<QueueItemDTO> peek() {
    Optional<QueueItemEntity> queueItem = QueueItemEntity.find("order by createdAt asc")
        .firstResultOptional();
    return queueItem.map(queueItemMapper::toDTO);
  }

  /**
   * Removes an item from the queue.
   *
   * @param queueItemId The ID of the item to remove.
   */
  public void remove(String queueItemId) {
    QueueItemEntity.deleteById(queueItemId);
  }

  /**
   * Marks an item as processed locally (i.e. sent to InfluxDB).
   *
   * @param queueItemId The ID of the item to mark as processed.
   */
  public void markProcessedLocal(String queueItemId) {
    QueueItemEntity queueItemEntity = QueueItemEntity.findById(queueItemId);
    queueItemEntity.setProcessedLocalAt(Instant.now());
    queueItemEntity.persist();
  }

  /**
   * Marks an item as processed by the core (i.e. sent to esthesis CORE).
   *
   * @param queueItemId The ID of the item to mark as processed.
   */
  public void markProcessedCore(String queueItemId) {
    QueueItemEntity queueItemEntity = QueueItemEntity.findById(queueItemId);
    queueItemEntity.setProcessedCoreAt(Instant.now());
    queueItemEntity.persist();
  }

  /**
   * Lists all items in the queue.
   *
   * @return All items in the queue.
   */
  public List<QueueItemDTO> list() {
    return queueItemMapper.toDTO(QueueItemEntity.listAll(Sort.by("createdAt")));
  }
}
