package esthesis.edge.modules.enedis.service;

import esthesis.common.avro.ELPEntry;
import esthesis.edge.modules.enedis.EnedisUtil;
import esthesis.edge.modules.enedis.config.EnedisProperties;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyConsumptionDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyConsumptionMaxPowerDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyProductionDTO;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * Service to map Enedis DTOs to ELP format.
 */
@ApplicationScoped
@RequiredArgsConstructor
public class EnedisELPMapperService {

  private final EnedisProperties enedisProperties;

  /**
   * Map EnedisDailyConsumptionDTO to ELP format.
   *
   * @param dto The DTO to map.
   * @return The ELP formatted string.
   */
  public String toELP(EnedisDailyConsumptionDTO dto) {
    return dto.getMeterReading().getIntervalReading().stream()
        .map(interval -> ELPEntry.builder()
            .category(enedisProperties.fetchTypes().dc().category())
            .date(EnedisUtil.ymdToInstant(interval.getDate()))
            .measurement(enedisProperties.fetchTypes().dc().measurement(),
                interval.getValue() + "i")
            .build().toString())
        .collect(Collectors.joining("\n"));
  }

  /**
   * Map EnedisDailyProductionDTO to ELP format.
   *
   * @param dto The DTO to map.
   * @return The ELP formatted string.
   */
  public String toELP(EnedisDailyProductionDTO dto) {
    return dto.getMeterReading().getIntervalReading().stream()
        .map(interval -> ELPEntry.builder()
            .category(enedisProperties.fetchTypes().dp().category())
            .date(EnedisUtil.ymdToInstant(interval.getDate()))
            .measurement(enedisProperties.fetchTypes().dp().measurement(),
                interval.getValue() + "i")
            .build().toString())
        .collect(Collectors.joining("\n"));
  }

  /**
   * Map EnedisDailyConsumptionMaxPowerDTO to ELP format.
   *
   * @param dto The DTO to map.
   * @return The ELP formatted string.
   */
  public String toELP(EnedisDailyConsumptionMaxPowerDTO dto) {
    return dto.getMeterReading().getIntervalReading().stream()
        .map(interval -> ELPEntry.builder()
            .category(enedisProperties.fetchTypes().dcmp().category())
            .date(interval.getDate())
            .measurement(enedisProperties.fetchTypes().dcmp().measurement(),
                interval.getValue() + "i")
            .build().toString())
        .collect(Collectors.joining("\n"));
  }
}
