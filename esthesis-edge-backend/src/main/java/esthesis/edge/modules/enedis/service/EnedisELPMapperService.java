package esthesis.edge.modules.enedis.service;

import esthesis.common.avro.ELPEntry;
import esthesis.edge.modules.enedis.EnedisUtil;
import esthesis.edge.modules.enedis.config.EnedisProperties;
import esthesis.edge.modules.enedis.dto.datahub.EnedisConsumptionLoadCurveDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyConsumptionDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyConsumptionMaxPowerDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisDailyProductionDTO;
import esthesis.edge.modules.enedis.dto.datahub.EnedisProductionLoadCurveDTO;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
            .date(EnedisUtil.isoInstantToInstant(interval.getDate()))
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
            .date(EnedisUtil.isoInstantToInstant(interval.getDate()))
            .measurement(enedisProperties.fetchTypes().dp().measurement(),
                interval.getValue() + "i")
            .build().toString())
        .collect(Collectors.joining("\n"));
  }

  /**
   * Map EnedisDailyConsumptionMaxPowerDTO to ELP format.
   * Note: With grandeurPhysique=TOUT the API returns arrays of values and dates within each
   * interval_reading (one entry per phase), where each value[i] corresponds to date[i]. With
   * PMA the same fields arrive as single-element lists. Entries with a missing or literal
   * "null" value/date are skipped (the Enedis sandbox returns such entries).
   *
   * @param dto The DTO to map.
   * @return The ELP formatted string.
   */
  public String toELP(EnedisDailyConsumptionMaxPowerDTO dto) {
    return dto.getMeterReading().getIntervalReading().stream()
        .flatMap(interval -> {
          List<String> values = interval.getValue();
          List<String> dates = interval.getDate();
          // Pair each value with its corresponding date.
          return IntStream.range(0, Math.min(values.size(), dates.size()))
              .filter(i -> isUsable(values.get(i)) && isUsable(dates.get(i)))
              .mapToObj(i -> ELPEntry.builder()
                  .category(enedisProperties.fetchTypes().dcmp().category())
                  .date(EnedisUtil.isoInstantToInstant(dates.get(i)))
                  .measurement(enedisProperties.fetchTypes().dcmp().measurement(),
                      values.get(i) + "i")
                  .build().toString());
        })
        .collect(Collectors.joining("\n"));
  }

  private static boolean isUsable(String value) {
    return value != null && !value.isBlank() && !"null".equals(value);
  }

  /**
   * Map EnedisConsumptionLoadCurveDTO to ELP format.
   *
   * @param dto The DTO to map.
   * @return The ELP formatted string.
   */
  public String toELP(EnedisConsumptionLoadCurveDTO dto) {
    return dto.getMeterReading().getIntervalReading().stream()
            .map(interval -> ELPEntry.builder()
                    .category(enedisProperties.fetchTypes().clc().category())
                    .date(EnedisUtil.isoInstantToInstant(interval.getDate()))
                    .measurement(enedisProperties.fetchTypes().clc().measurement(),
                            interval.getValue() + "i")
                    .build().toString())
            .collect(Collectors.joining("\n"));
  }

  /**
   * Map EnedisProductionLoadCurveDTO to ELP format.
   *
   * @param dto The DTO to map.
   * @return The ELP formatted string.
   */
  public String toELP(EnedisProductionLoadCurveDTO dto) {
    return dto.getMeterReading().getIntervalReading().stream()
            .map(interval -> ELPEntry.builder()
                    .category(enedisProperties.fetchTypes().plc().category())
                    .date(EnedisUtil.isoInstantToInstant(interval.getDate()))
                    .measurement(enedisProperties.fetchTypes().plc().measurement(),
                            interval.getValue() + "i")
                    .build().toString())
            .collect(Collectors.joining("\n"));
  }
}
