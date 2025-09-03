package esthesis.edge.modules.deddie.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * A DTO representing the search parameters for Deddie energy curves.
 */
@Data
@Accessors(chain = true)
public class DeddieCurvesSearchParametersDTO {
    private int analysisType;
    private String classType;
    private String fromDate;
    private String supplyNumber;
    private String taxNumber;
    private String toDate;
}
