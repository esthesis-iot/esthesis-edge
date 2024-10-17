package esthesis.edge.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a queue item entity.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "queue_item", indexes = {
    @Index(name = "idx_hid_processed_at", columnList = "hardware_id, processed_local_at, processed_core_at")})
public class QueueItemEntity extends PanacheEntityBase {

  @Id
  @NotBlank
  private String id;

  // The data object that is queued.
  @Lob
  @Column(length = 16777215, nullable = false, name = "data_object")
  private String dataObject;

  // The date the entry was created.
  @Column(nullable = false, name = "created_at")
  private Instant createdAt;

  // The date the entry was synchronised with InfluxDB.
  @Column(name = "processed_local_at")
  private Instant processedLocalAt;

  // The date the entry was synchronised with esthesis CORE.
  @Column(name = "processed_core_at")
  private Instant processedCoreAt;

  // The hardware ID of the device that generated the data.
  @Column(nullable = false, name = "hardware_id")
  private String hardwareId;

  /**
   * Finds all queue items that have not been synchronised locally (i.e. to InfluxDB).
   *
   * @return A list of queue items that have not been processed locally.
   */
  public static List<QueueItemEntity> findNotProcessedLocal() {
    return find("processedLocalAt IS NULL order by createdAt").list();
  }

  /**
   * Finds all queue items that have not been synchronised with esthesis CORE.
   *
   * @return A list of queue items that have not been processed with esthesis CORE.
   */
  public static List<String> findDistinctHardwareIdsWithNotProcessedCore() {
    return find("SELECT DISTINCT hardwareId FROM QueueItemEntity WHERE processedCoreAt IS NULL "
        + "order by hardwareId")
        .project(String.class)
        .list();
  }

  /**
   * Finds all queue items that have not been synchronised with esthesis CORE for a specific
   * hardware ID.
   *
   * @param hardwareId The hardware ID to filter by.
   * @return A list of queue items that have not been synchronised with esthesis CORE for the given
   * hardware ID.
   */
  public static List<QueueItemEntity> findByHardwareIdNotProcessedCore(String hardwareId) {
    return find("hardwareId = ?1 AND processedCoreAt IS NULL order by createdAt",
        hardwareId).list();
  }
}
