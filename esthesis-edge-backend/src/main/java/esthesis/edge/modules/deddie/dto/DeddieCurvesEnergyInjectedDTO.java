package esthesis.edge.modules.deddie.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * A DTO representing the energy injected curves data from Deddie API.
 */

@Data
@ToString
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class DeddieCurvesEnergyInjectedDTO {

    private CurveSearchParametersDTO curveSearchParameters;
    private List<Curve> curves;

    @Data
    public static class Curve{
        private Boolean certifiedFlag;
        private String consumption;
        private String meterDate;
    }

    @Data
    public static class CurveSearchParametersDTO {
        private int analysisType;
        private String classType;
        private String fromDate;
        private String supplyNumber;
        private String taxNumber;
        private String toDate;
        private boolean hourAnalysisFlag; // This property is obsolete in API V2.
        private boolean confirmedDataFlag; // This property is obsolete in API V2.
    }
}
