package esthesis.edge.modules.enedis.dto.datahub;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * A DTO representing the daily consumption data maximum power received from Enedis.
 *
 * <p>The response shape depends on the grandeurPhysique request parameter: with PMA the
 * flow_direction/unit/value/date fields are scalars, with TOUT (three-phase meters) they are
 * arrays holding PMA, PMA1, PMA2 and PMA3. ACCEPT_SINGLE_VALUE_AS_ARRAY lets both shapes
 * deserialize into lists.
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
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> flowDirection;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> unit;
    private String aggregate;
  }

  @Data
  public static class IntervalReading {

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> value;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> date;
  }
}
