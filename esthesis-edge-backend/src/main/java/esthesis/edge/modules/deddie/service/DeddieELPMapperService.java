package esthesis.edge.modules.deddie.service;

import esthesis.common.avro.ELPEntry;
import esthesis.edge.modules.deddie.DeddieUtil;
import esthesis.edge.modules.deddie.config.DeddieProperties;
import esthesis.edge.modules.deddie.dto.DeddieCurvesActiveConsumptionDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesEnergyInjectedDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesEnergyProducedDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesReactivePowerDTO;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DeddieELPMapperService {

    private final DeddieProperties deddieProperties;


    /**
     * Map DeddieCurvesActiveConsumptionDTO to ELP format.
     *
     * @param dto The DTO to map.
     * @return The ELP formatted string.
     */
    public String toELP(DeddieCurvesActiveConsumptionDTO dto) {
        return toELP(dto.getCurves(),
                deddieProperties.fetchTypes().cac().category(),
                deddieProperties.fetchTypes().cac().measurement(),
                DeddieCurvesActiveConsumptionDTO.Curve::getConsumption,
                DeddieCurvesActiveConsumptionDTO.Curve::getMeterDate);
    }

    /**
     * Map DeddieCurvesReactivePowerDTO to ELP format.
     *
     * @param dto The DTO to map.
     * @return The ELP formatted string.
     */
    public String toELP(DeddieCurvesReactivePowerDTO dto) {
        return toELP(dto.getCurves(),
                deddieProperties.fetchTypes().crp().category(),
                deddieProperties.fetchTypes().crp().measurement(),
                DeddieCurvesReactivePowerDTO.Curve::getConsumption,
                DeddieCurvesReactivePowerDTO.Curve::getMeterDate);
    }

    /**
     * Map DeddieCurvesEnergyProducedDTO to ELP format.
     *
     * @param dto The DTO to map.
     * @return The ELP formatted string.
     */
    public String toELP(DeddieCurvesEnergyProducedDTO dto) {
        return toELP(dto.getCurves(),
                deddieProperties.fetchTypes().cep().category(),
                deddieProperties.fetchTypes().cep().measurement(),
                DeddieCurvesEnergyProducedDTO.Curve::getConsumption,
                DeddieCurvesEnergyProducedDTO.Curve::getMeterDate);
    }

    /**
     * Map DeddieCurvesEnergyInjectedDTO to ELP format.
     *
     * @param dto The DTO to map.
     * @return The ELP formatted string.
     */
    public String toELP(DeddieCurvesEnergyInjectedDTO dto) {
        return toELP(dto.getCurves(),
                deddieProperties.fetchTypes().cei().category(),
                deddieProperties.fetchTypes().cei().measurement(),
                DeddieCurvesEnergyInjectedDTO.Curve::getConsumption,
                DeddieCurvesEnergyInjectedDTO.Curve::getMeterDate);
    }


    private <T> String toELP(List<T> curves,
                             String category,
                             String measurement,
                             Function<T, String> consumptionExtractor,
                             Function<T, String> meterDateExtractor) {
        if (curves == null || curves.isEmpty()) {
            return "";
        }

        return curves.stream()
            .map(curve -> toELPEntry(
                        category,
                        measurement,
                        consumptionExtractor.apply(curve),
                        meterDateExtractor.apply(curve)))
                .flatMap(Optional::stream)
                .collect(Collectors.joining("\n"));
    }

        private Optional<String> toELPEntry(String category,
                                        String measurement,
                                        String consumption,
                                        String meterDate) {
        if (StringUtils.isBlank(consumption)) {
            log.warn("Skipping DEDDIE measurement '{}' row at meterDate '{}' due to blank consumption value.",
                measurement, meterDate);
            return Optional.empty();
        }

        String normalizedConsumption = consumption.trim();
        try {
            Float.parseFloat(normalizedConsumption);
        } catch (NumberFormatException e) {
            log.warn("Skipping DEDDIE measurement '{}' row at meterDate '{}' due to invalid consumption value '{}'.",
                measurement, meterDate, consumption);
            return Optional.empty();
        }

        if (StringUtils.isBlank(meterDate)) {
            log.warn("Skipping DEDDIE measurement '{}' row due to missing meterDate.", measurement);
            return Optional.empty();
        }

        try {
            Instant timestamp = DeddieUtil.toInstant(meterDate);
            return Optional.of(ELPEntry.builder()
                    .category(category)
                    .date(timestamp)
                    .measurement(measurement, normalizedConsumption + "f")
                    .build()
                    .toString());
        } catch (Exception e) {
            log.warn("Skipping DEDDIE measurement '{}' row due to invalid meterDate '{}'.", measurement, meterDate);
            return Optional.empty();
        }
    }

}
