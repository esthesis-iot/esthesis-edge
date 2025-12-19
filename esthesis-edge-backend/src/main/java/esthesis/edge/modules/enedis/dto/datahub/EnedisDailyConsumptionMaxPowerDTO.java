package esthesis.edge.modules.enedis.dto.datahub;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * A DTO representing the daily consumption data maximum power received from Enedis.
 */
@Data
@ToString
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class EnedisDailyConsumptionMaxPowerDTO {

  @JsonProperty("meter_reading")
  private EnedisDailyConsumptionMaxPowerDTO.MeterReading meterReading;

  @Data
  public static class MeterReading {

    @JsonProperty("usage_point_id")
    private String usagePointId;

    private String start;
    private String end;
    private String quality;

    @JsonProperty("reading_type")
    private EnedisDailyConsumptionMaxPowerDTO.ReadingType readingType;

    @JsonProperty("interval_reading")
    private List<EnedisDailyConsumptionMaxPowerDTO.IntervalReading> intervalReading;
  }

  @Data
  public static class ReadingType {

    @JsonProperty("measurement_kind")
    private String measurementKind;

    @JsonProperty("measuring_period")
    private String measuringPeriod;

    @JsonProperty("flow_direction")
    private List<String> flowDirection;

    private List<String> unit;
    private String aggregate;
  }

  @Data
  public static class IntervalReading {

    private List<String> value;
    private List<String> date;
  }
}
