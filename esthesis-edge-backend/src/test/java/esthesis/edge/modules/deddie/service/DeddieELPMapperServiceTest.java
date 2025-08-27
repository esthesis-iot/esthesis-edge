package esthesis.edge.modules.deddie.service;

import esthesis.edge.modules.deddie.config.DeddieProperties;
import esthesis.edge.modules.deddie.dto.DeddieCurvesActiveConsumptionDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesEnergyInjectedDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesEnergyProducedDTO;
import esthesis.edge.modules.deddie.dto.DeddieCurvesReactivePowerDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class DeddieELPMapperServiceTest {

    @Inject
    DeddieELPMapperService deddieELPMapperService;

    @Inject
    DeddieProperties deddieProperties;

    @Test
    void toELPCAC() {
        DeddieCurvesActiveConsumptionDTO dto = new DeddieCurvesActiveConsumptionDTO();
        dto.setCurveSearchParameters(new DeddieCurvesActiveConsumptionDTO.CurveSearchParametersDTO());
        dto.setCurves(List.of(new DeddieCurvesActiveConsumptionDTO.Curve()));
        dto.getCurves().getFirst().setCertifiedFlag(false);
        dto.getCurves().getFirst().setConsumption("1.1");
        dto.getCurves().getFirst().setMeterDate("18/08/2025 00:15");

        String elp = deddieELPMapperService.toELP(dto);
        assertNotNull(elp);
        assertEquals(deddieProperties.fetchTypes().cac().category() + " "
                + deddieProperties.fetchTypes().cac().measurement() + "=1.1f "
                + "2025-08-18T00:15:00Z", elp);
    }

    @Test
    void toELPCRP() {
        DeddieCurvesReactivePowerDTO dto = new DeddieCurvesReactivePowerDTO();
        dto.setCurveSearchParameters(new DeddieCurvesReactivePowerDTO.CurveSearchParametersDTO());
        dto.setCurves(List.of(new DeddieCurvesReactivePowerDTO.Curve()));
        dto.getCurves().getFirst().setCertifiedFlag(false);
        dto.getCurves().getFirst().setConsumption("1.2");
        dto.getCurves().getFirst().setMeterDate("18/08/2025 00:15");

        String elp = deddieELPMapperService.toELP(dto);
        assertNotNull(elp);
        assertEquals(deddieProperties.fetchTypes().crp().category() + " "
                + deddieProperties.fetchTypes().crp().measurement() + "=1.2f "
                + "2025-08-18T00:15:00Z", elp);
    }

    @Test
    void toELPCEP() {
        DeddieCurvesEnergyProducedDTO dto = new DeddieCurvesEnergyProducedDTO();
        dto.setCurveSearchParameters(new DeddieCurvesEnergyProducedDTO.CurveSearchParametersDTO());
        dto.setCurves(List.of(new DeddieCurvesEnergyProducedDTO.Curve()));
        dto.getCurves().getFirst().setCertifiedFlag(false);
        dto.getCurves().getFirst().setConsumption("1.3");
        dto.getCurves().getFirst().setMeterDate("18/08/2025 00:15");

        String elp = deddieELPMapperService.toELP(dto);
        assertNotNull(elp);
        assertEquals(deddieProperties.fetchTypes().cep().category() + " "
                + deddieProperties.fetchTypes().cep().measurement() + "=1.3f "
                + "2025-08-18T00:15:00Z", elp);
    }

    @Test
    void toELPCEI() {
        DeddieCurvesEnergyInjectedDTO dto = new DeddieCurvesEnergyInjectedDTO();
        dto.setCurveSearchParameters(new DeddieCurvesEnergyInjectedDTO.CurveSearchParametersDTO());
        dto.setCurves(List.of(new DeddieCurvesEnergyInjectedDTO.Curve()));
        dto.getCurves().getFirst().setCertifiedFlag(false);
        dto.getCurves().getFirst().setConsumption("1.4");
        dto.getCurves().getFirst().setMeterDate("18/08/2025 00:15");

        String elp = deddieELPMapperService.toELP(dto);
        assertNotNull(elp);
        assertEquals(deddieProperties.fetchTypes().cei().category() + " "
                + deddieProperties.fetchTypes().cei().measurement() + "=1.4f "
                + "2025-08-18T00:15:00Z", elp);
    }
}