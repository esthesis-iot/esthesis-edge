package esthesis.edge.modules.enedis.dto.datahub;

import com.fasterxml.jackson.annotation.JsonProperty;
import esthesis.edge.modules.enedis.EnedisUtil;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
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

    private String unit;
    private String aggregate;
  }

  @Data
  public static class IntervalReading {

    private String value;
    private Instant date;

    public void setDate(String date) {
      this.date = EnedisUtil.yyyyMMddTHHmmssSSSZToInstantToInstant(date);
    }
  }
}
