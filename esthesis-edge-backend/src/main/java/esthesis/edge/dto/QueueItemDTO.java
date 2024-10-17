package esthesis.edge.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * A DTO representing an item queued in EDGE's database.
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class QueueItemDTO {

  private String id;
  private String dataObject;
  private Instant createdAt;
  private Instant processedLocalAt;
  private Instant processedCoreAt;
  private String hardwareId;
}
