package esthesis.edge.modules.fronius;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.edge.modules.fronius.dto.FroniusPowerFlowRealtimeDataDTO;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.SneakyThrows;

@ApplicationScoped
public class FroniusTestUtils {

    ObjectMapper mapper = new ObjectMapper();

    public FroniusTestUtils() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    @SneakyThrows
    public FroniusPowerFlowRealtimeDataDTO createFroniusPowerFlowRealtimeDataDTO() {
        return mapper.readValue(
                getFroniusPowerFlowRealtimeDataDTOJson(),
                FroniusPowerFlowRealtimeDataDTO.class);
    }

    private String getFroniusPowerFlowRealtimeDataDTOJson() {
        return """
                {
                   "Body" : {
                      "Data" : {
                         "Inverters" : {
                            "1" : {
                               "DT" : 110,
                               "E_Day" : 15912,
                               "E_Total" : 22526400,
                               "E_Year" : 1217760.375,
                               "P" : 4380
                            }
                         },
                         "Site" : {
                            "E_Day" : 15912,
                            "E_Total" : 22526400,
                            "E_Year" : 1217760.375,
                            "Meter_Location" : "unknown",
                            "Mode" : "produce-only",
                            "P_Akku" : null,
                            "P_Grid" : null,
                            "P_Load" : null,
                            "P_PV" : 4380,
                            "rel_Autonomy" : null,
                            "rel_SelfConsumption" : null
                         },
                         "Version" : "12"
                      }
                   },
                   "Head" : {
                      "RequestArguments" : {},
                      "Status" : {
                         "Code" : 0,
                         "Reason" : "",
                         "UserMessage" : ""
                      },
                      "Timestamp" : "2025-03-13T13:01:41+02:00"
                   }
                }
                """;
    }
}
