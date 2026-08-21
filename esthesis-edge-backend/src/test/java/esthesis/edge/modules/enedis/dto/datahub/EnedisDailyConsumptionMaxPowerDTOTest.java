package esthesis.edge.modules.enedis.dto.datahub;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The daily_consumption_max_power response is polymorphic on the grandeurPhysique request
 * parameter: PMA returns scalars, TOUT returns arrays (PMA, PMA1, PMA2, PMA3). Both shapes must
 * deserialize.
 */
class EnedisDailyConsumptionMaxPowerDTOTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void deserializePmaScalars() throws JsonProcessingException {
    EnedisDailyConsumptionMaxPowerDTO dto = objectMapper.readValue("""
        {
          "meter_reading": {
            "usage_point_id": "99999999999999",
            "start": "2025-05-01T00:00:00.000Z",
            "end": "2025-07-14T00:00:00.000Z",
            "quality": "BRUT",
            "reading_type": {
              "flow_direction": "forward",
              "measurement_kind": "power",
              "measuring_period": "P1D",
              "unit": "VA",
              "aggregate": "maximum"
            },
            "interval_reading": [
              {
                "value": "9656",
                "date": "2025-05-01T04:12:00.000Z"
              },
              {
                "value": "null",
                "date": "2025-05-02T00:00:00.000Z"
              }
            ]
          }
        }
        """, EnedisDailyConsumptionMaxPowerDTO.class);

    assertEquals(List.of("forward"), dto.getMeterReading().getReadingType().getFlowDirection());
    assertEquals(List.of("VA"), dto.getMeterReading().getReadingType().getUnit());
    assertEquals(List.of("9656"), dto.getMeterReading().getIntervalReading().get(0).getValue());
    assertEquals(List.of("2025-05-01T04:12:00.000Z"),
        dto.getMeterReading().getIntervalReading().get(0).getDate());
    assertEquals(List.of("null"), dto.getMeterReading().getIntervalReading().get(1).getValue());
  }

  @Test
  void deserializeToutArrays() throws JsonProcessingException {
    EnedisDailyConsumptionMaxPowerDTO dto = objectMapper.readValue("""
        {
          "meter_reading": {
            "usage_point_id": "11111111111111",
            "start": "2025-05-01T00:00:00.000Z",
            "end": "2025-07-14T00:00:00.000Z",
            "quality": "BRUT",
            "reading_type": {
              "flow_direction": ["forward", "forward", "forward", "forward"],
              "measurement_kind": "power",
              "measuring_period": "P1D",
              "unit": ["VA", "VA", "VA", "VA"],
              "aggregate": "maximum"
            },
            "interval_reading": [
              {
                "value": ["9656", "9724", "9298", "9904"],
                "date": [
                  "2025-05-01T04:12:00.000Z",
                  "2025-05-01T04:12:00.000Z",
                  "2025-05-01T04:12:00.000Z",
                  "2025-05-01T04:12:00.000Z"
                ]
              }
            ]
          }
        }
        """, EnedisDailyConsumptionMaxPowerDTO.class);

    assertEquals(4, dto.getMeterReading().getReadingType().getUnit().size());
    assertEquals(List.of("9656", "9724", "9298", "9904"),
        dto.getMeterReading().getIntervalReading().get(0).getValue());
    assertEquals(4, dto.getMeterReading().getIntervalReading().get(0).getDate().size());
  }
}
