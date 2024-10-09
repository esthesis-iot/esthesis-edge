package esthesis.edge.impl.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

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

  @Length(max = 4096)
  @Column(length = 4096, nullable = false, name = "data_object")
  private String dataObject;

  @Column(nullable = false, name = "created_at")
  private Instant createdAt;

  @Column(name = "processed_local_at")
  private Instant processedLocalAt;

  @Column(name = "processed_core_at")
  private Instant processedCoreAt;

  @Column(nullable = false, name = "hardware_id")
  private String hardwareId;

  public static List<QueueItemEntity> findNotProcessedLocal() {
    return find("processedLocalAt IS NULL").list();
  }

  public static List<QueueItemEntity> findNotProcessedCore() {
    return find("processedCoreAt IS NULL").list();
  }

}
