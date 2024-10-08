package esthesis.edge.impl.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
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
    @Index(name = "idx_hid_processed_at", columnList = "hardwareId, processedAt")})
public class QueueItemEntity extends PanacheEntityBase {

  @Id
  @NotBlank
  private String id;

  @Length(max = 4096)
  @Column(length = 4096, nullable = false)
  private String dataObject;

  @Column(nullable = false)
  private Instant createdAt;

  private Instant processedAt;

  @Column(nullable = false)
  private String hardwareId;

}
