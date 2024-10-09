package esthesis.edge.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

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
