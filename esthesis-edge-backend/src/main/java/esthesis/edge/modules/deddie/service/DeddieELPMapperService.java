package esthesis.edge.modules.deddie.service;

import esthesis.common.avro.ELPEntry;
import esthesis.edge.modules.deddie.DeddieUtil;
import esthesis.edge.modules.deddie.config.DeddieProperties;
import esthesis.edge.modules.deddie.dto.DeddieCurvesActiveConsumptionDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesEnergyInjectedDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesEnergyProducedDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesReactivePowerDTO;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;

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
        return dto.getCurves().stream().map(curve -> ELPEntry.builder()
                        .category(deddieProperties.fetchTypes().cac().category())
                        .date(DeddieUtil.toInstant(curve.getMeterDate()))
                        .measurement(deddieProperties.fetchTypes().cac().measurement(),
                                curve.getConsumption() + "f")
                        .build().toString())
                .collect(Collectors.joining("\n"));
    }

    /**
     * Map DeddieCurvesReactivePowerDTO to ELP format.
     *
     * @param dto The DTO to map.
     * @return The ELP formatted string.
     */
    public String toELP(DeddieCurvesReactivePowerDTO dto) {
        return dto.getCurves().stream().map(curve -> ELPEntry.builder()
                        .category(deddieProperties.fetchTypes().crp().category())
                        .date(DeddieUtil.toInstant(curve.getMeterDate()))
                        .measurement(deddieProperties.fetchTypes().crp().measurement(),
                                curve.getConsumption() + "f")
                        .build().toString())
                .collect(Collectors.joining("\n"));
    }

    /**
     * Map DeddieCurvesEnergyProducedDTO to ELP format.
     *
     * @param dto The DTO to map.
     * @return The ELP formatted string.
     */
    public String toELP(DeddieCurvesEnergyProducedDTO dto) {
        return dto.getCurves().stream().map(curve -> ELPEntry.builder()
                        .category(deddieProperties.fetchTypes().cep().category())
                        .date(DeddieUtil.toInstant(curve.getMeterDate()))
                        .measurement(deddieProperties.fetchTypes().cep().measurement(),
                                curve.getConsumption() + "f")
                        .build().toString())
                .collect(Collectors.joining("\n"));
    }

    /**
     * Map DeddieCurvesEnergyInjectedDTO to ELP format.
     *
     * @param dto The DTO to map.
     * @return The ELP formatted string.
     */
    public String toELP(DeddieCurvesEnergyInjectedDTO dto) {
        return dto.getCurves().stream().map(curve -> ELPEntry.builder()
                        .category(deddieProperties.fetchTypes().cei().category())
                        .date(DeddieUtil.toInstant(curve.getMeterDate()))
                        .measurement(deddieProperties.fetchTypes().cei().measurement(),
                                curve.getConsumption() + "f")
                        .build().toString())
                .collect(Collectors.joining("\n"));
    }


}
