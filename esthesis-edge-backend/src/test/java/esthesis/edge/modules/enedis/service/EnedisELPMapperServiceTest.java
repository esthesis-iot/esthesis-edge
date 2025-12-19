package esthesis.edge.modules.enedis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import esthesis.edge.modules.enedis.config.EnedisProperties;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyConsumptionDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyConsumptionMaxPowerDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyProductionDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
class EnedisELPMapperServiceTest {

  @Inject
  EnedisELPMapperService enedisELPMapperService;

  @Inject
  EnedisProperties enedisProperties;

  @Test
  void toELPDC() {
    EnedisDailyConsumptionDTO dto = new EnedisDailyConsumptionDTO();
    dto.setMeterReading(new EnedisDailyConsumptionDTO.MeterReading());
    dto.getMeterReading()
        .setIntervalReading(List.of(new EnedisDailyConsumptionDTO.IntervalReading()));
    dto.getMeterReading().getIntervalReading().get(0).setDate("2021-01-01T23:59:59.000Z");
    dto.getMeterReading().getIntervalReading().get(0).setValue("1");

    String elp = enedisELPMapperService.toELP(dto);
    assertNotNull(elp);
    assertEquals(enedisProperties.fetchTypes().dc().category() + " "
        + enedisProperties.fetchTypes().dc().measurement() + "=1i "
        + "2021-01-01T23:59:59Z", elp);
  }

  @Test
  void toELPDP() {
    EnedisDailyProductionDTO dto = new EnedisDailyProductionDTO();
    dto.setMeterReading(new EnedisDailyProductionDTO.MeterReading());
    dto.getMeterReading()
        .setIntervalReading(List.of(new EnedisDailyProductionDTO.IntervalReading()));
    dto.getMeterReading().getIntervalReading().get(0).setDate("2021-01-01T23:59:59.000Z");
    dto.getMeterReading().getIntervalReading().get(0).setValue("1");

    String elp = enedisELPMapperService.toELP(dto);
    assertNotNull(elp);
    assertEquals(enedisProperties.fetchTypes().dp().category() + " "
        + enedisProperties.fetchTypes().dp().measurement() + "=1i "
        + "2021-01-01T23:59:59Z", elp);
  }

  @Test
  void toELPDCMP() {
    EnedisDailyConsumptionMaxPowerDTO dto = new EnedisDailyConsumptionMaxPowerDTO();
    dto.setMeterReading(new EnedisDailyConsumptionMaxPowerDTO.MeterReading());
    EnedisDailyConsumptionMaxPowerDTO.IntervalReading intervalReading =
        new EnedisDailyConsumptionMaxPowerDTO.IntervalReading();
    // The API returns arrays of values and dates
    intervalReading.setValue(List.of("1", "2"));
    intervalReading.setDate(List.of("2019-05-06", "2019-05-07"));
    dto.getMeterReading().setIntervalReading(List.of(intervalReading));

    String elp = enedisELPMapperService.toELP(dto);
    assertNotNull(elp);
    // Should produce two ELP entries, one for each value/date pair
    String expectedLine1 = enedisProperties.fetchTypes().dcmp().category() + " "
        + enedisProperties.fetchTypes().dcmp().measurement() + "=1i "
        + "2019-05-06T23:59:59Z";
    String expectedLine2 = enedisProperties.fetchTypes().dcmp().category() + " "
        + enedisProperties.fetchTypes().dcmp().measurement() + "=2i "
        + "2019-05-07T23:59:59Z";
    assertEquals(expectedLine1 + "\n" + expectedLine2, elp);
  }
}
