package esthesis.edge.api.service;

import esthesis.edge.api.dto.QueueItemDTO;
import java.util.List;
import java.util.Optional;

public interface DataService {

  /**
   * Queue a data item.
   *
   * @param queueItemDTO the data item to queue.
   */
  void queue(QueueItemDTO queueItemDTO);

  /**
   * Peek at the next item in the queue in a FIFO manner. The item is not removed from the queue.
   *
   * @return the next item in the queue, or empty if the queue is empty.
   */
  Optional<QueueItemDTO> peek();

  /**
   * Remove an item from the queue.
   *
   * @param queueItemId the ID of the item to remove.
   */
  void remove(String queueItemId);

  /**
   * Mark an item as processed.
   *
   * @param queueItemId the ID of the item to mark as processed.
   */
  void markProcessed(String queueItemId);

  /**
   * List all items in the queue.
   *
   * @return a list of all items in the queue.
   */
  List<QueueItemDTO> list();
}
