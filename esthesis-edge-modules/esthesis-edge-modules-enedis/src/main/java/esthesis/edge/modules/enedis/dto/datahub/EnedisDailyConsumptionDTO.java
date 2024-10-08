package esthesis.edge.modules.enedis.dto.datahub;

import com.fasterxml.jackson.annotation.JsonProperty;
import esthesis.edge.modules.enedis.EnedisUtil;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class EnedisDailyConsumptionDTO {

  @JsonProperty("meter_reading")
  private MeterReading meterReading;

  @Data
  public static class MeterReading {

    @JsonProperty("usage_point_id")
    private String usagePointId;

    private String start;
    private String end;
    private String quality;

    @JsonProperty("reading_type")
    private ReadingType readingType;

    @JsonProperty("interval_reading")
    private List<IntervalReading> intervalReading;
  }

  @Data
  public static class ReadingType {

    @JsonProperty("measurement_kind")
    private String measurementKind;

    @JsonProperty("measuring_period")
    private String measuringPeriod;

    private String unit;
    private String aggregate;
  }

  @Data
  public static class IntervalReading {

    private String value;
    private String date;
  }

  /**
   * Convert the DTO to ELP format.
   *
   * @return Daily Consumption in ELP format.
   */
  public String toELP() {
    StringBuilder elp = new StringBuilder();
    for (IntervalReading interval : meterReading.getIntervalReading()) {
      elp.append(String.format("energy dq=%d %s\n",
          Integer.parseInt(interval.getValue()),
          EnedisUtil.StringDataToELPData(interval.getDate())));
    }

    return elp.toString();
  }
}
