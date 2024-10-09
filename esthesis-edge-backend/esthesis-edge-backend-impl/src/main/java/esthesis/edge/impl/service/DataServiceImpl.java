package esthesis.edge.impl.service;

import esthesis.edge.api.dto.QueueItemDTO;
import esthesis.edge.api.service.DataService;
import esthesis.edge.impl.mapper.QueueItemMapper;
import esthesis.edge.impl.model.QueueItemEntity;
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
public class DataServiceImpl implements DataService {

  private final QueueItemMapper queueItemMapper;

  @Override
  public void queue(QueueItemDTO queueItemDTO) {
    QueueItemEntity queueItemEntity = new QueueItemEntity();
    queueItemEntity.setId(queueItemDTO.getId());
    queueItemEntity.setCreatedAt(queueItemDTO.getCreatedAt());
    queueItemEntity.setDataObject(queueItemDTO.getDataObject());
    queueItemEntity.setHardwareId(queueItemDTO.getHardwareId());
    queueItemEntity.persist();
  }

  @Override
  public Optional<QueueItemDTO> peek() {
    Optional<QueueItemEntity> queueItem = QueueItemEntity.find("order by createdAt asc")
        .firstResultOptional();
    return queueItem.map(queueItemMapper::toDTO);
  }

  @Override
  public void remove(String queueItemId) {
    QueueItemEntity.deleteById(queueItemId);
  }

  @Override
  public void markProcessedLocal(String queueItemId) {
    QueueItemEntity queueItemEntity = QueueItemEntity.findById(queueItemId);
    queueItemEntity.setProcessedLocalAt(Instant.now());
    queueItemEntity.persist();
  }

  @Override
  public void markProcessedCore(String queueItemId) {
    QueueItemEntity queueItemEntity = QueueItemEntity.findById(queueItemId);
    queueItemEntity.setProcessedCoreAt(Instant.now());
    queueItemEntity.persist();
  }

  @Override
  public List<QueueItemDTO> list() {
    return queueItemMapper.toDTO(QueueItemEntity.listAll(Sort.by("createdAt")));
  }
}
